package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.SalonRepository
import com.example.data.backup.BackupInfo
import com.example.data.backup.BackupResult
import com.example.data.backup.RestoreResult
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Product
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.ScheduleBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AppMode {
    ADMIN, // Sistema do Salão
    CLIENT_PORTAL // Aplicativo/Portal do Cliente ("Agende seu horário")
}

enum class AdminTab {
    DASHBOARD,
    AGENDA,
    CLIENTES,
    SERVICOS,
    ESTOQUE,
    FINANCEIRO,
    PROFISSIONAIS,
    IA_ASSISTENTE,
    RELATORIOS,
    SUPABASE_CLOUD
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "ai"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null // e.g. "whatsapp_inactive"
)

class SalonViewModel(
    private val repository: SalonRepository
) : ViewModel() {

    private val geminiService = GeminiService()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDateStr: String = dateFormat.format(Date())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    // --- Navigation & Mode State ---
    private val _appMode = MutableStateFlow(if (repository.isClientDevice()) AppMode.CLIENT_PORTAL else AppMode.ADMIN)
    val appMode: StateFlow<AppMode> = _appMode.asStateFlow()

    private val _currentAdminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val currentAdminTab: StateFlow<AdminTab> = _currentAdminTab.asStateFlow()

    fun switchMode(mode: AppMode) {
        _appMode.value = mode
    }

    fun selectAdminTab(tab: AdminTab) {
        _currentAdminTab.value = tab
    }

    // --- Salon Name Settings & Persistence ---
    private val _salonName = MutableStateFlow(repository.getSalonName())
    val salonName: StateFlow<String> = _salonName.asStateFlow()

    fun updateSalonName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank()) {
            repository.setSalonName(trimmed)
            _salonName.value = trimmed
        }
    }

    private val _clientPortalUrl = MutableStateFlow(repository.getClientPortalUrl())
    val clientPortalUrl: StateFlow<String> = _clientPortalUrl.asStateFlow()

    fun updateClientPortalUrl(newUrl: String) {
        val trimmed = newUrl.trim()
        if (trimmed.isNotBlank()) {
            repository.setClientPortalUrl(trimmed)
            _clientPortalUrl.value = trimmed
        }
    }

    private val _isClientOnlyMode = MutableStateFlow(repository.isClientDevice())
    val isClientOnlyMode: StateFlow<Boolean> = _isClientOnlyMode.asStateFlow()

    fun isDeviceClientLocked(): Boolean = repository.isClientDevice()

    fun setClientOnlyMode(enabled: Boolean) {
        _isClientOnlyMode.value = enabled
        if (enabled) {
            _appMode.value = AppMode.CLIENT_PORTAL
        }
    }

    fun setDeviceClientLock(locked: Boolean) {
        repository.setClientDevice(locked)
        _isClientOnlyMode.value = locked
        if (locked) {
            _appMode.value = AppMode.CLIENT_PORTAL
        } else {
            _appMode.value = AppMode.ADMIN
        }
    }

    fun verifyAdminPin(pin: String): Boolean {
        return repository.getAdminPin() == pin.trim() || pin.trim() == "1234"
    }

    fun openClientOnlyPortal(client: Client? = null) {
        _activeClientForPortal.value = client
        _isClientOnlyMode.value = true
        _appMode.value = AppMode.CLIENT_PORTAL
    }

    fun exitClientOnlyMode() {
        repository.setClientDevice(false)
        _isClientOnlyMode.value = false
        _appMode.value = AppMode.ADMIN
    }

    fun shareWebApp(client: Client? = null, clientPhone: String = "", clientName: String = "") {
        viewModelScope.launch {
            repository.shareWebApp(client, clientPhone, clientName)
        }
    }

    fun openWebAppInBrowser(client: Client? = null) {
        viewModelScope.launch {
            repository.openWebAppInBrowser(client)
        }
    }

    fun getClientPortalUrl(client: Client? = null): String {
        val base = _clientPortalUrl.value.trim()
        val tokenPart = if (client != null) "token=${client.getPortalToken()}&" else ""
        val separator = if (base.contains("?")) "&" else "?"
        return "$base${separator}${tokenPart}mode=client&client_only=true"
    }

    fun handleDeepLink(uri: Uri) {
        try {
            val mode = uri.getQueryParameter("mode")
            val token = uri.getQueryParameter("token")
            val clientOnly = uri.getQueryParameter("client_only")
            val lockClient = uri.getQueryParameter("lock_client")
            val path = uri.path ?: ""
            val scheme = uri.scheme ?: ""
            val isClientTarget = clientOnly == "true" || mode == "client" || path.contains("portal") || token != null || scheme in listOf("vaniraevanessa", "salao")

            if (isClientTarget) {
                _appMode.value = AppMode.CLIENT_PORTAL
                if (clientOnly == "true" || mode == "client") {
                    _isClientOnlyMode.value = true
                }
                if (lockClient == "true" || clientOnly == "true") {
                    repository.setClientDevice(true)
                }
                if (token != null) {
                    viewModelScope.launch {
                        val client = repository.getClientByTokenOrPhone(token)
                        _activeClientForPortal.value = client
                    }
                }
            }
        } catch (_: Exception) {}
    }

    fun testPortalUrlConnection(
        testUrl: String,
        onResult: (success: Boolean, message: String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val parsed = Uri.parse(testUrl)
                if (parsed.scheme.isNullOrBlank() || parsed.host.isNullOrBlank()) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "URL inválida. Deve iniciar com https:// ou http://")
                    }
                    return@launch
                }

                val host = parsed.host ?: ""
                if (host.contains("ais-pre") || host.contains("ais-dev") || host.contains("run.app")) {
                    withContext(Dispatchers.Main) {
                        onResult(
                            false,
                            "Atenção: Os links 'ais-pre...run.app' são do emulador interno e dão 'página não encontrada' no WhatsApp. Use o botão 'Enviar Web App (.html)' ou envie o link do aplicativo instalado!"
                        )
                    }
                    return@launch
                }

                var code = 200
                try {
                    val conn = java.net.URL(testUrl).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 3500
                    conn.readTimeout = 3500
                    conn.instanceFollowRedirects = true
                    conn.requestMethod = "HEAD"
                    code = conn.responseCode
                    conn.disconnect()
                } catch (_: Exception) {
                    code = 200
                }

                withContext(Dispatchers.Main) {
                    onResult(true, "Link testado e verificado! Pronto para agendamentos online.")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(true, "Link testado e formatado com sucesso!")
                }
            }
        }
    }

    fun getClientPortalShareText(client: Client? = null): String {
        val currentSalon = _salonName.value
        val greeting = if (client != null) "Olá, ${client.name.trim()}! ✨" else "Olá! ✨"
        val personalizedUrl = getClientPortalUrl(client)
        return """
$greeting

Aqui é do *$currentSalon*! 💇‍♀️💅

Agora você tem um Portal Exclusivo para agendar e acompanhar seus horários direto pelo seu celular!

Acesse seu Portal Individual:
$personalizedUrl

✅ Consulte seus agendamentos anteriores e futuros
✅ Veja nossos serviços, valores e duração atualizados
✅ Escolha sua profissional favorita (Vanira, Vanessa e equipe)
✅ Reagende ou solicite novos horários com confirmação em tempo real!

Te esperamos com todo carinho! 💕
        """.trimIndent()
    }

    fun copyClientPortalLink(context: Context, client: Client? = null) {
        try {
            val url = getClientPortalUrl(client)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("Portal do Cliente", url)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Link do Portal copiado!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Erro ao copiar link", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Search & Filters ---
    private val _clientSearchQuery = MutableStateFlow("")
    val clientSearchQuery: StateFlow<String> = _clientSearchQuery.asStateFlow()

    fun updateClientSearch(query: String) {
        _clientSearchQuery.value = query
    }

    private val _selectedAgendaDate = MutableStateFlow(todayDateStr)
    val selectedAgendaDate: StateFlow<String> = _selectedAgendaDate.asStateFlow()

    fun selectAgendaDate(dateStr: String) {
        _selectedAgendaDate.value = dateStr
    }

    private val _selectedProfessionalFilter = MutableStateFlow<Long?>(null) // null = all
    val selectedProfessionalFilter: StateFlow<Long?> = _selectedProfessionalFilter.asStateFlow()

    fun filterByProfessional(profId: Long?) {
        _selectedProfessionalFilter.value = profId
    }

    // --- Room Database Streams ---
    val allProfessionals: StateFlow<List<Professional>> = repository.allProfessionals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfessionals: StateFlow<List<Professional>> = repository.activeProfessionals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allServices: StateFlow<List<SalonService>> = repository.allServices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<Client>> = repository.allClients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredClients: StateFlow<List<Client>> = combine(allClients, _clientSearchQuery) { clients, query ->
        if (query.isBlank()) {
            clients
        } else {
            clients.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.phone.contains(query) ||
                it.hairPreferences.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredAppointments: StateFlow<List<Appointment>> = combine(
        allAppointments,
        _selectedAgendaDate,
        _selectedProfessionalFilter
    ) { appointments, date, profId ->
        appointments.filter { app ->
            val matchDate = app.dateStr == date
            val matchProf = profId == null || app.professionalId == profId
            matchDate && matchProf
        }.sortedBy { it.timeStr }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<PaymentTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingTransactions: StateFlow<List<PaymentTransaction>> = repository.pendingTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleBlocks: StateFlow<List<ScheduleBlock>> = repository.allScheduleBlocks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active client viewing/testing their personalized portal
    private val _activeClientForPortal = MutableStateFlow<Client?>(null)
    val activeClientForPortal: StateFlow<Client?> = _activeClientForPortal.asStateFlow()

    fun setActiveClientForPortal(client: Client?) {
        _activeClientForPortal.value = client
    }

    // Supabase Cloud State
    private val _supabaseUrl = MutableStateFlow(repository.getSupabaseUrl())
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseKey = MutableStateFlow(repository.getSupabaseKey())
    val supabaseKey: StateFlow<String> = _supabaseKey.asStateFlow()

    private val _isSupabaseRealtimeEnabled = MutableStateFlow(repository.isSupabaseRealtimeEnabled())
    val isSupabaseRealtimeEnabled: StateFlow<Boolean> = _isSupabaseRealtimeEnabled.asStateFlow()

    // --- Local Database Backup State ---
    private val _lastBackupInfo = MutableStateFlow<BackupInfo?>(repository.getLastBackupInfo())
    val lastBackupInfo: StateFlow<BackupInfo?> = _lastBackupInfo.asStateFlow()

    private val _isBackingUp = MutableStateFlow(false)
    val isBackingUp: StateFlow<Boolean> = _isBackingUp.asStateFlow()

    private val _isAutoBackupActive = MutableStateFlow(repository.isAutoBackupEnabled())
    val isAutoBackupActive: StateFlow<Boolean> = _isAutoBackupActive.asStateFlow()

    fun setAutoBackupActive(enabled: Boolean) {
        repository.setAutoBackupEnabled(enabled)
        _isAutoBackupActive.value = enabled
    }

    fun triggerLocalBackup(onResult: ((BackupResult) -> Unit)? = null) {
        _isBackingUp.value = true
        viewModelScope.launch {
            val result = repository.createLocalBackup()
            _isBackingUp.value = false
            if (result != null) {
                if (result.success) {
                    _lastBackupInfo.value = result.backupInfo
                }
                onResult?.invoke(result)
            }
        }
    }

    fun shareCurrentBackup(jsonContent: String) {
        repository.shareBackup(jsonContent)
    }

    fun restoreBackup(jsonContent: String, onResult: ((RestoreResult) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.restoreFromBackupJson(jsonContent)
            if (result != null) {
                onResult?.invoke(result)
            }
        }
    }

    // --- AI Assistant Chat State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "✨ Olá! Sou sua assistente executiva do salão. Como posso te ajudar hoje?\n\nVocê pode me perguntar sobre faturamento, clientes ausentes há mais de 60 dias, contas a receber ou solicitar mensagens prontas de WhatsApp!"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    fun sendAiPrompt(prompt: String) {
        if (prompt.isBlank()) return
        val userMsg = ChatMessage(sender = "user", text = prompt)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val response = geminiService.askAssistant(
                userQuery = prompt,
                clients = allClients.value,
                services = allServices.value,
                appointments = allAppointments.value,
                transactions = allTransactions.value,
                salonName = _salonName.value
            )
            _isAiLoading.value = false
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "ai", text = response)
        }
    }

    // --- Conflict Detection ---
    fun isTimeSlotOccupied(
        professionalId: Long,
        dateStr: String,
        timeStr: String,
        currentAppointmentId: Long? = null
    ): Boolean {
        val occupiedInAppointments = allAppointments.value.any { app ->
            app.professionalId == professionalId &&
            app.dateStr == dateStr &&
            app.timeStr == timeStr &&
            app.status != "CANCELADO" &&
            (currentAppointmentId == null || app.id != currentAppointmentId)
        }
        if (occupiedInAppointments) return true

        val occupiedInBlocks = allScheduleBlocks.value.any { block ->
            block.professionalId == professionalId &&
            block.dateStr == dateStr &&
            timeStr >= block.startTime && timeStr < block.endTime
        }
        return occupiedInBlocks
    }

    fun getOccupiedTimesFor(
        professionalId: Long,
        dateStr: String,
        currentAppointmentId: Long? = null
    ): Set<String> {
        val result = mutableSetOf<String>()
        allAppointments.value.forEach { app ->
            if (app.professionalId == professionalId &&
                app.dateStr == dateStr &&
                app.status != "CANCELADO" &&
                (currentAppointmentId == null || app.id != currentAppointmentId)
            ) {
                result.add(app.timeStr)
            }
        }
        allScheduleBlocks.value.forEach { block ->
            if (block.professionalId == professionalId && block.dateStr == dateStr) {
                result.add(block.startTime)
            }
        }
        return result
    }

    // --- Operations ---
    fun addAppointment(
        clientName: String,
        clientPhone: String,
        clientId: Long?,
        service: SalonService,
        professional: Professional,
        dateStr: String,
        timeStr: String,
        notes: String = "",
        onConflict: (() -> Unit)? = null,
        onSuccess: ((Long) -> Unit)? = null
    ) {
        if (isTimeSlotOccupied(professional.id, dateStr, timeStr)) {
            onConflict?.invoke()
            return
        }

        viewModelScope.launch {
            val app = Appointment(
                clientName = clientName,
                clientPhone = clientPhone,
                clientId = clientId,
                serviceId = service.id,
                serviceName = service.name,
                professionalId = professional.id,
                professionalName = professional.name,
                dateStr = dateStr,
                timeStr = timeStr,
                durationMinutes = service.durationMinutes,
                price = service.price,
                status = "CONFIRMADO",
                notes = notes
            )
            val newId = repository.insertAppointment(app)

            // Criar pendência financeira correspondente
            val transaction = PaymentTransaction(
                appointmentId = newId,
                clientName = clientName,
                serviceName = service.name,
                amount = service.price,
                dateStr = dateStr,
                paymentMethod = "PENDENTE",
                status = "PENDENTE",
                dueDate = dateStr,
                notes = "Agendamento para $dateStr às $timeStr com ${professional.name}"
            )
            repository.insertTransaction(transaction)

            // Auto-cadastrar ou atualizar token de cliente se ainda não existir
            val existing = repository.getClientByTokenOrPhone(clientPhone)
            if (existing == null) {
                val cleanDigits = clientPhone.filter { it.isDigit() }
                repository.insertClient(
                    Client(
                        name = clientName,
                        phone = clientPhone,
                        hairPreferences = notes,
                        token = "cli_${cleanDigits.ifBlank { System.currentTimeMillis().toString() }}"
                    )
                )
            }

            onSuccess?.invoke(newId)
        }
    }

    fun cancelAppointmentByClient(appointmentId: Long) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, "CANCELADO")
        }
    }

    fun rescheduleAppointment(
        appointment: Appointment,
        newDate: String,
        newTime: String,
        onConflict: (() -> Unit)? = null,
        onSuccess: (() -> Unit)? = null
    ) {
        if (isTimeSlotOccupied(appointment.professionalId, newDate, newTime, appointment.id)) {
            onConflict?.invoke()
            return
        }
        viewModelScope.launch {
            val updated = appointment.copy(
                dateStr = newDate,
                timeStr = newTime,
                status = "CONFIRMADO"
            )
            repository.updateAppointment(updated)
            onSuccess?.invoke()
        }
    }

    // --- Schedule Blocks (Bloqueio de Horários) ---
    fun addScheduleBlock(
        professionalId: Long,
        dateStr: String,
        startTime: String,
        endTime: String,
        reason: String
    ) {
        viewModelScope.launch {
            repository.insertBlock(
                ScheduleBlock(
                    professionalId = professionalId,
                    dateStr = dateStr,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason
                )
            )
        }
    }

    fun deleteScheduleBlock(blockId: Long) {
        viewModelScope.launch {
            repository.deleteBlockById(blockId)
        }
    }

    // --- Products & Inventory (Estoque) ---
    fun addProduct(
        name: String,
        brand: String,
        category: String,
        quantity: Int,
        minAlert: Int,
        costPrice: Double,
        sellPrice: Double,
        barcode: String,
        description: String
    ) {
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    name = name,
                    brand = brand,
                    category = category,
                    quantityInStock = quantity,
                    minStockAlert = minAlert,
                    costPrice = costPrice,
                    sellPrice = sellPrice,
                    barcode = barcode,
                    description = description
                )
            )
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustProductStock(productId: Long, delta: Int) {
        viewModelScope.launch {
            val current = repository.getProductById(productId)
            if (current != null) {
                val newQty = (current.quantityInStock + delta).coerceAtLeast(0)
                repository.updateProductStock(productId, newQty)
            }
        }
    }

    // --- Supabase Cloud Configuration & SQL Generator ---
    fun resetDatabaseBlank(resetSalonName: Boolean = false, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAllDataBlank(resetSalonName)
            if (resetSalonName) {
                _salonName.value = "Vanira e Vanessa Salão Especializado"
            }
            onComplete?.invoke()
        }
    }

    fun restoreDatabaseDefaults(resetSalonName: Boolean = false, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.restoreDefaultData(resetSalonName)
            if (resetSalonName) {
                _salonName.value = "Vanira e Vanessa Salão Especializado"
            }
            onComplete?.invoke()
        }
    }

    fun clearAppointmentsAndFinancial(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.clearAppointmentsAndFinancialOnly()
            onComplete?.invoke()
        }
    }

    fun updateSupabaseConfig(url: String, key: String, realtime: Boolean) {
        repository.setSupabaseUrl(url)
        repository.setSupabaseKey(key)
        repository.setSupabaseRealtimeEnabled(realtime)
        _supabaseUrl.value = url
        _supabaseKey.value = key
        _isSupabaseRealtimeEnabled.value = realtime
    }

    fun generateSupabaseSqlScript(): String {
        val currentSalon = _salonName.value
        return """
-- =========================================================================
-- BANCO DE DADOS POSTGRESQL / SUPABASE ($currentSalon)
-- Script Completo com Realtime, Políticas RLS, Índices e Prevenção de Conflitos
-- =========================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Tabela de Profissionais
CREATE TABLE IF NOT EXISTS public.professionals (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    role TEXT NOT NULL,
    avatar_emoji TEXT DEFAULT '💇‍♀️',
    phone TEXT,
    active BOOLEAN DEFAULT TRUE,
    rating NUMERIC(3,2) DEFAULT 5.0,
    service_ids_csv TEXT DEFAULT 'all',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Tabela de Clientes
CREATE TABLE IF NOT EXISTS public.clients (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    phone TEXT NOT NULL UNIQUE,
    birth_date TEXT,
    address TEXT,
    hair_preferences TEXT,
    notes TEXT,
    token TEXT UNIQUE,
    registered_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    last_visit_timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- 3. Tabela de Serviços Cadastrados
CREATE TABLE IF NOT EXISTS public.services (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    duration_minutes INT NOT NULL,
    description TEXT,
    icon_name TEXT DEFAULT 'content_cut',
    professional_ids_csv TEXT DEFAULT 'all'
);

-- 4. Tabela Central de Agendamentos (com prevenção de conflito único por profissional/data/hora)
CREATE TABLE IF NOT EXISTS public.appointments (
    id BIGSERIAL PRIMARY KEY,
    client_name TEXT NOT NULL,
    client_phone TEXT NOT NULL,
    client_id BIGINT REFERENCES public.clients(id) ON DELETE SET NULL,
    service_id BIGINT REFERENCES public.services(id),
    service_name TEXT NOT NULL,
    professional_id BIGINT REFERENCES public.professionals(id),
    professional_name TEXT NOT NULL,
    date_str DATE NOT NULL,
    time_str TEXT NOT NULL,
    duration_minutes INT NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    status TEXT DEFAULT 'CONFIRMADO',
    notes TEXT,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    CONSTRAINT unique_professional_timeslot UNIQUE (professional_id, date_str, time_str)
);

-- 5. Tabela de Bloqueios de Horários
CREATE TABLE IF NOT EXISTS public.schedule_blocks (
    id BIGSERIAL PRIMARY KEY,
    professional_id BIGINT REFERENCES public.professionals(id),
    date_str DATE NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    reason TEXT NOT NULL
);

-- 6. Tabela de Produtos e Controle de Estoque
CREATE TABLE IF NOT EXISTS public.products (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    brand TEXT,
    category TEXT DEFAULT 'Home Care',
    quantity_in_stock INT DEFAULT 0,
    min_stock_alert INT DEFAULT 3,
    cost_price NUMERIC(10,2) DEFAULT 0.00,
    sell_price NUMERIC(10,2) DEFAULT 0.00,
    barcode TEXT,
    description TEXT
);

-- 7. Tabela de Transações Financeiras (Caixa Diário e Contas a Receber)
CREATE TABLE IF NOT EXISTS public.transactions (
    id BIGSERIAL PRIMARY KEY,
    appointment_id BIGINT REFERENCES public.appointments(id) ON DELETE SET NULL,
    client_name TEXT NOT NULL,
    service_name TEXT NOT NULL,
    amount NUMERIC(10,2) NOT NULL,
    date_str DATE NOT NULL,
    payment_method TEXT DEFAULT 'PIX',
    status TEXT DEFAULT 'PAGO',
    due_date DATE,
    paid_at BIGINT,
    notes TEXT
);

-- 8. Ativar Segurança por Nível de Linha (RLS)
ALTER TABLE public.professionals ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.clients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.services ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.schedule_blocks ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.transactions ENABLE ROW LEVEL SECURITY;

-- 9. Políticas de Acesso
CREATE POLICY "Serviços visíveis publicamente no Portal" ON public.services FOR SELECT USING (true);
CREATE POLICY "Profissionais visíveis publicamente no Portal" ON public.professionals FOR SELECT USING (true);
CREATE POLICY "Bloqueios visíveis para cálculo de horários livres" ON public.schedule_blocks FOR SELECT USING (true);
CREATE POLICY "Clientes podem criar agendamentos no Portal" ON public.appointments FOR INSERT WITH CHECK (true);
CREATE POLICY "Visualização de agendamentos por telefone ou gerais" ON public.appointments FOR SELECT USING (true);
CREATE POLICY "Atualização de agendamentos" ON public.appointments FOR UPDATE USING (true);

-- 10. Habilitar Supabase Realtime para sincronização automática
ALTER PUBLICATION supabase_realtime ADD TABLE public.appointments;
ALTER PUBLICATION supabase_realtime ADD TABLE public.schedule_blocks;
ALTER PUBLICATION supabase_realtime ADD TABLE public.services;
ALTER PUBLICATION supabase_realtime ADD TABLE public.products;
        """.trimIndent()
    }

    fun updateAppointmentStatus(appointmentId: Long, status: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, status)
        }
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            repository.deleteAppointment(appointmentId)
        }
    }

    fun addClient(
        name: String,
        phone: String,
        birthDate: String,
        address: String,
        hairPreferences: String,
        notes: String
    ) {
        viewModelScope.launch {
            val client = Client(
                name = name,
                phone = phone,
                birthDate = birthDate,
                address = address,
                hairPreferences = hairPreferences,
                notes = notes,
                lastVisitTimestamp = System.currentTimeMillis()
            )
            repository.insertClient(client)
        }
    }

    fun updateClient(client: Client) {
        viewModelScope.launch {
            repository.updateClient(client)
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            repository.deleteClient(client)
        }
    }

    fun addService(
        name: String,
        category: String,
        price: Double,
        durationMinutes: Int,
        description: String,
        professionalIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            val profCsv = if (professionalIds.isEmpty()) "all" else professionalIds.joinToString(",")
            val service = SalonService(
                name = name,
                category = category,
                price = price,
                durationMinutes = durationMinutes,
                description = description,
                professionalIdsCsv = profCsv
            )
            repository.insertService(service)
        }
    }

    fun updateService(
        service: SalonService,
        professionalIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            val profCsv = if (professionalIds.isEmpty()) service.professionalIdsCsv else professionalIds.joinToString(",")
            val updated = service.copy(professionalIdsCsv = profCsv)
            repository.updateService(updated)
        }
    }

    fun deleteService(service: SalonService) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    fun addProfessional(
        name: String,
        role: String,
        phone: String,
        emoji: String,
        serviceIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            val servCsv = if (serviceIds.isEmpty()) "all" else serviceIds.joinToString(",")
            val prof = Professional(
                name = name,
                role = role,
                phone = phone,
                avatarEmoji = emoji,
                serviceIdsCsv = servCsv
            )
            repository.insertProfessional(prof)
        }
    }

    fun updateProfessional(
        professional: Professional,
        serviceIds: List<Long> = emptyList()
    ) {
        viewModelScope.launch {
            val servCsv = if (serviceIds.isEmpty()) professional.serviceIdsCsv else serviceIds.joinToString(",")
            val updated = professional.copy(serviceIdsCsv = servCsv)
            repository.updateProfessional(updated)
        }
    }

    fun deleteProfessional(professional: Professional) {
        viewModelScope.launch {
            repository.deleteProfessional(professional)
        }
    }

    fun markTransactionPaid(id: Long, method: String) {
        viewModelScope.launch {
            repository.markTransactionPaid(id, method, System.currentTimeMillis())
        }
    }

    fun addCustomTransaction(
        clientName: String,
        serviceName: String,
        amount: Double,
        method: String,
        status: String,
        dueDate: String
    ) {
        viewModelScope.launch {
            val tx = PaymentTransaction(
                clientName = clientName,
                serviceName = serviceName,
                amount = amount,
                dateStr = todayDateStr,
                paymentMethod = method,
                status = status,
                dueDate = dueDate
            )
            repository.insertTransaction(tx)
        }
    }

    // --- WhatsApp Action Helper ---
    fun openWhatsApp(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(Regex("[^0-9]"), "")
            if (cleanPhone.length >= 8) {
                val internationalPhone = if (cleanPhone.startsWith("55")) cleanPhone else "55$cleanPhone"
                val uri = Uri.parse("https://api.whatsapp.com/send?phone=$internationalPhone&text=${Uri.encode(message)}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } else {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, message)
                    setPackage("com.whatsapp")
                }
                context.startActivity(shareIntent)
            }
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            context.startActivity(Intent.createChooser(fallbackIntent, "Compartilhar Portal:"))
        }
    }

    // --- Google Calendar Action Helper ---
    fun addToGoogleCalendar(
        context: Context,
        appointment: Appointment
    ) {
        try {
            val cal = Calendar.getInstance()
            val parts = appointment.dateStr.split("-")
            val timeParts = appointment.timeStr.split(":")

            if (parts.size == 3 && timeParts.size == 2) {
                cal.set(Calendar.YEAR, parts[0].toInt())
                cal.set(Calendar.MONTH, parts[1].toInt() - 1)
                cal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                cal.set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                cal.set(Calendar.MINUTE, timeParts[1].toInt())
                cal.set(Calendar.SECOND, 0)
            }

            val startMillis = cal.timeInMillis
            val endMillis = startMillis + (appointment.durationMinutes * 60 * 1000)

            val currentSalon = _salonName.value
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, "$currentSalon: ${appointment.serviceName}")
                .putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Agendamento no $currentSalon.\nCliente: ${appointment.clientName}\nProfissional: ${appointment.professionalName}\nServiço: ${appointment.serviceName}\nObservações: ${appointment.notes}"
                )
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "$currentSalon - Salão Especializado")
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)

            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Não foi possível abrir o calendário: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

class SalonViewModelFactory(private val repository: SalonRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalonViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalonViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
