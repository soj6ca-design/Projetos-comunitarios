package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.executesService
import com.example.data.model.isExecutedBy
import com.example.ui.theme.BellaGold
import com.example.ui.theme.BellaPrimary
import com.example.ui.theme.BellaSecondary
import java.text.NumberFormat
import java.util.Locale

enum class ClientPortalTab {
    AGENDAR,
    MEUS_AGENDAMENTOS,
    ENVIAR_WEB_APP,
    CONSULTORA_IA,
    MEUS_DADOS
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClientPortalScreen(
    services: List<SalonService>,
    professionals: List<Professional>,
    allAppointments: List<Appointment>,
    todayDate: String,
    salonName: String = "Vanira e Vanessa Salão Especializado",
    activeClient: Client? = null,
    occupiedTimesProvider: (professionalId: Long, dateStr: String) -> Set<String>,
    onBookAppointment: (
        clientName: String,
        clientPhone: String,
        service: SalonService,
        professional: Professional,
        dateStr: String,
        timeStr: String,
        notes: String
    ) -> Unit,
    onCancelAppointment: (Long) -> Unit,
    onRescheduleAppointment: (Appointment, String, String) -> Unit,
    onAddToCalendar: (Appointment) -> Unit,
    onWhatsAppClick: (String, String) -> Unit,
    onSharePortalClick: (() -> Unit)? = null,
    onSaveClientProfile: ((Client) -> Unit)? = null,
    clients: List<Client> = emptyList(),
    onShareWebApp: ((Client?, phone: String, name: String) -> Unit)? = null,
    onOpenWebAppInBrowser: ((Client?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("pt", "BR")) }

    var currentPortalTab by remember { mutableStateOf(ClientPortalTab.AGENDAR) }

    // Booking Steps State (1: Serviço, 2: Profissional, 3: Data & Horário, 4: Seus Dados, 5: Sucesso)
    var step by remember { mutableIntStateOf(1) }

    var selectedService by remember { mutableStateOf<SalonService?>(services.firstOrNull()) }
    var selectedProfessional by remember { mutableStateOf<Professional?>(professionals.firstOrNull()) }
    var selectedDate by remember { mutableStateOf(todayDate) }
    var selectedTime by remember { mutableStateOf("10:00") }

    var clientName by remember(activeClient) { mutableStateOf(activeClient?.name ?: "") }
    var clientPhone by remember(activeClient) { mutableStateOf(activeClient?.phone ?: "") }
    var clientNotes by remember(activeClient) { mutableStateOf(activeClient?.hairPreferences ?: "") }

    // Enviar Web App Tab State
    var shareClientSearchQuery by remember { mutableStateOf("") }
    var selectedShareClient by remember(activeClient) { mutableStateOf<Client?>(activeClient) }
    var shareCustomName by remember(activeClient) { mutableStateOf(activeClient?.name ?: "") }
    var shareCustomPhone by remember(activeClient) { mutableStateOf(activeClient?.phone ?: "") }

    var createdAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showConflictAlert by remember { mutableStateOf(false) }

    // Reschedule State Dialog
    var appointmentToReschedule by remember { mutableStateOf<Appointment?>(null) }

    // Client appointments filter
    var clientFilterPhone by remember(activeClient) { mutableStateOf(activeClient?.phone ?: "") }

    val myAppointments = remember(allAppointments, clientFilterPhone) {
        if (clientFilterPhone.isBlank()) {
            allAppointments.take(10)
        } else {
            val cleanFilter = clientFilterPhone.filter { it.isDigit() }
            allAppointments.filter { app ->
                val cleanApp = app.clientPhone.filter { it.isDigit() }
                cleanApp.contains(cleanFilter) || cleanFilter.contains(cleanApp) ||
                        app.clientName.contains(clientFilterPhone, ignoreCase = true)
            }
        }
    }

    // Dynamic Occupied Times for Selected Professional & Date (Prevents simultaneous conflict)
    val occupiedTimes = remember(selectedProfessional, selectedDate, allAppointments) {
        val profId = selectedProfessional?.id ?: 0L
        occupiedTimesProvider(profId, selectedDate)
    }

    // Sync selected service
    LaunchedEffect(services) {
        if (selectedService == null || services.none { it.id == selectedService?.id }) {
            selectedService = services.firstOrNull()
        }
    }

    // Service Filtering & Search in Step 1
    var selectedCategory by remember { mutableStateOf("Todos") }
    var serviceSearchQuery by remember { mutableStateOf("") }

    val categories = remember(services) {
        listOf("Todos") + services.map { it.category }.distinct().sorted()
    }

    val displayedServices = remember(services, selectedCategory, serviceSearchQuery) {
        services.filter { service ->
            val matchesCategory = selectedCategory == "Todos" || service.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = serviceSearchQuery.isBlank() ||
                    service.name.contains(serviceSearchQuery, ignoreCase = true) ||
                    service.description.contains(serviceSearchQuery, ignoreCase = true) ||
                    service.category.contains(serviceSearchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    val times = listOf(
        "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00", "17:30"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Portal Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = salonName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                        Text(
                            text = if (activeClient != null) "Olá, ${activeClient.name}! • Portal Individual" else "Portal do Cliente • Tempo Real",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        )
                    }

                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ao Vivo",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (onSharePortalClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = onSharePortalClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF1B5E20)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_share_portal_whatsapp")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF25D366),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enviar Link do Portal via WhatsApp para Celular",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Sub-navigation Tabs: Agendar, Meus Agendamentos, Enviar Web App, Consultora IA, Meus Dados
        ScrollableTabRow(
            selectedTabIndex = currentPortalTab.ordinal,
            edgePadding = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Tab(
                selected = currentPortalTab == ClientPortalTab.AGENDAR,
                onClick = { currentPortalTab = ClientPortalTab.AGENDAR },
                text = { Text("🗓️ Agendar", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = currentPortalTab == ClientPortalTab.MEUS_AGENDAMENTOS,
                onClick = { currentPortalTab = ClientPortalTab.MEUS_AGENDAMENTOS },
                text = { Text("📋 Meus Horários", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = currentPortalTab == ClientPortalTab.ENVIAR_WEB_APP,
                onClick = { currentPortalTab = ClientPortalTab.ENVIAR_WEB_APP },
                text = { Text("🚀 Enviar Web App", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = currentPortalTab == ClientPortalTab.CONSULTORA_IA,
                onClick = { currentPortalTab = ClientPortalTab.CONSULTORA_IA },
                text = { Text("✨ Dúvidas & IA", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = currentPortalTab == ClientPortalTab.MEUS_DADOS,
                onClick = { currentPortalTab = ClientPortalTab.MEUS_DADOS },
                text = { Text("👤 Meu Perfil", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (currentPortalTab) {
            ClientPortalTab.AGENDAR -> {
                // Steps Progress Indicator
                if (step in 1..4) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StepIndicator(stepNumber = 1, title = "Serviços", active = step == 1, completed = step > 1)
                        StepIndicator(stepNumber = 2, title = "Profissional", active = step == 2, completed = step > 2)
                        StepIndicator(stepNumber = 3, title = "Data/Hora", active = step == 3, completed = step > 3)
                        StepIndicator(stepNumber = 4, title = "Confirmar", active = step == 4, completed = step > 4)
                    }
                }

                when (step) {
                    1 -> {
                        // Step 1: Escolha do Serviço Cadastrado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. Serviços Cadastrados (${services.size}):",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Valores atualizados",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = serviceSearchQuery,
                            onValueChange = { serviceSearchQuery = it },
                            placeholder = { Text("Buscar serviço por nome...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (serviceSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { serviceSearchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        if (categories.size > 2) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                categories.forEach { category ->
                                    FilterChip(
                                        selected = selectedCategory == category,
                                        onClick = { selectedCategory = category },
                                        label = { Text(category, fontSize = 12.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        if (services.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum serviço cadastrado no momento.", color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(displayedServices) { service ->
                                    val isSelected = selectedService?.id == service.id
                                    val executingProfs = remember(service, professionals) {
                                        professionals.filter { prof ->
                                            prof.executesService(service.id) && service.isExecutedBy(prof.id)
                                        }
                                    }

                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(
                                                width = if (isSelected) 2.dp else 1.dp,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                            .clickable { selectedService = service },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = service.name,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = service.category,
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                        Surface(
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = RoundedCornerShape(6.dp)
                                                        ) {
                                                            Text(
                                                                text = "${service.durationMinutes} min",
                                                                fontSize = 10.sp,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = currencyFormat.format(service.price),
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                )
                                            }

                                            if (service.description.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = service.description,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                                                )
                                            }

                                            if (executingProfs.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = "Profissionais: ${executingProfs.joinToString { it.name }}",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = { step = 2 },
                            enabled = selectedService != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Continuar para Profissional", fontWeight = FontWeight.Bold)
                        }
                    }

                    2 -> {
                        // Step 2: Escolha de Profissional
                        val eligibleProfs = remember(selectedService, professionals) {
                            val s = selectedService
                            if (s == null) professionals
                            else {
                                val matched = professionals.filter { p ->
                                    p.executesService(s.id) && s.isExecutedBy(p.id)
                                }
                                if (matched.isNotEmpty()) matched else professionals
                            }
                        }

                        Text(
                            text = "2. Escolha sua Especialista:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(eligibleProfs) { prof ->
                                val isSelected = selectedProfessional?.id == prof.id
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { selectedProfessional = prof },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(prof.avatarEmoji, fontSize = 20.sp)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prof.name,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = prof.role,
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.tertiaryContainer)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Star, contentDescription = null, tint = BellaGold, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("${prof.rating}", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Voltar")
                            }
                            Button(
                                onClick = { step = 3 },
                                enabled = selectedProfessional != null,
                                modifier = Modifier.weight(1.5f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Data e Horário com PREVENÇÃO DE CONFLITO
                        Text(
                            text = "3. Escolha Dia e Horário Disponível:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Horários ocupados são bloqueados automaticamente pelo sistema.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text("Selecione o Dia:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "Hoje" to todayDate,
                                        "Amanhã" to "2026-09-28",
                                        "Terça" to "2026-09-29",
                                        "Quarta" to "2026-09-30",
                                        "Quinta" to "2026-10-01",
                                        "Sexta" to "2026-10-02",
                                        "Sábado" to "2026-10-03"
                                    ).forEach { (label, date) ->
                                        val isSelected = selectedDate == date
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                                .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                                .clickable { selectedDate = date }
                                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = label,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = date.substringAfter("-"),
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Horários Livres:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF2E7D32), CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Livre", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFFD32F2F), CircleShape))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Ocupado", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    times.chunked(3).forEach { rowTimes ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            rowTimes.forEach { time ->
                                                val isOccupied = occupiedTimes.contains(time)
                                                val isSelected = selectedTime == time && !isOccupied

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            when {
                                                                isOccupied -> Color(0xFFFFEBEE)
                                                                isSelected -> MaterialTheme.colorScheme.primary
                                                                else -> MaterialTheme.colorScheme.surface
                                                            }
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = when {
                                                                isOccupied -> Color(0xFFFFCDD2)
                                                                isSelected -> MaterialTheme.colorScheme.primary
                                                                else -> MaterialTheme.colorScheme.outlineVariant
                                                            },
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .clickable(enabled = !isOccupied) {
                                                            selectedTime = time
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = time,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when {
                                                                isOccupied -> Color(0xFFC62828)
                                                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            },
                                                            textDecoration = if (isOccupied) TextDecoration.LineThrough else TextDecoration.None
                                                        )
                                                        if (isOccupied) {
                                                            Text(
                                                                text = "Ocupado",
                                                                fontSize = 9.sp,
                                                                color = Color(0xFFC62828),
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 2 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Voltar")
                            }
                            Button(
                                onClick = {
                                    if (occupiedTimes.contains(selectedTime)) {
                                        showConflictAlert = true
                                    } else {
                                        step = 4
                                    }
                                },
                                enabled = !occupiedTimes.contains(selectedTime),
                                modifier = Modifier.weight(1.5f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Continuar", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    4 -> {
                        // Step 4: Dados da Cliente & Confirmação
                        Text(
                            text = "4. Seus Dados de Contato:",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = clientName,
                                    onValueChange = { clientName = it },
                                    label = { Text("Seu Nome Completo *") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = clientPhone,
                                    onValueChange = { clientPhone = it },
                                    label = { Text("Seu WhatsApp com DDD *") },
                                    placeholder = { Text("Ex: 11987654321") },
                                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = clientNotes,
                                    onValueChange = { clientNotes = it },
                                    label = { Text("Preferências capilares ou observações") },
                                    placeholder = { Text("Ex: Cabelo descolorido, prefere água morna...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 2,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Resumo do Agendamento:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("• Salão: $salonName")
                                        Text("• Serviço: ${selectedService?.name} (${currencyFormat.format(selectedService?.price ?: 0.0)})")
                                        Text("• Profissional: ${selectedProfessional?.name}")
                                        Text("• Horário: $selectedDate às $selectedTime")
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "⚡ Sincronizado instantaneamente na base central do salão.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 3 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Voltar")
                            }
                            Button(
                                onClick = {
                                    val serv = selectedService ?: return@Button
                                    val prof = selectedProfessional ?: return@Button
                                    if (clientName.isNotBlank() && clientPhone.isNotBlank()) {
                                        onBookAppointment(
                                            clientName,
                                            clientPhone,
                                            serv,
                                            prof,
                                            selectedDate,
                                            selectedTime,
                                            clientNotes
                                        )
                                        createdAppointment = Appointment(
                                            clientName = clientName,
                                            clientPhone = clientPhone,
                                            serviceId = serv.id,
                                            serviceName = serv.name,
                                            professionalId = prof.id,
                                            professionalName = prof.name,
                                            dateStr = selectedDate,
                                            timeStr = selectedTime,
                                            durationMinutes = serv.durationMinutes,
                                            price = serv.price,
                                            notes = clientNotes
                                        )
                                        step = 5
                                    }
                                },
                                enabled = clientName.isNotBlank() && clientPhone.isNotBlank(),
                                modifier = Modifier.weight(1.6f).height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Confirmar Horário", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    5 -> {
                        // Step 5: Sucesso & Confirmação com WhatsApp e Google Agenda
                        val app = createdAppointment
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(36.dp))
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Agendamento Confirmado!",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "Seu horário foi sincronizado e reservado com sucesso no $salonName.",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                if (app != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text("💇‍♀️ ${app.serviceName}", fontWeight = FontWeight.Bold)
                                            Text("👤 Profissional: ${app.professionalName}")
                                            Text("📅 Data: ${app.dateStr} às ${app.timeStr}")
                                            Text("💰 Valor: ${currencyFormat.format(app.price)}")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { onAddToCalendar(app) },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Adicionar ao Google Agenda")
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    FilledTonalButton(
                                        onClick = {
                                            val msg = "Olá, acabei de agendar pelo aplicativo meu horário para ${app.serviceName} com ${app.professionalName} no dia ${app.dateStr} às ${app.timeStr}. Meu nome é ${app.clientName}!"
                                            onWhatsAppClick(app.clientPhone, msg)
                                        },
                                        modifier = Modifier.fillMaxWidth().height(46.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFE8F5E9),
                                            contentColor = Color(0xFF1B5E20)
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("💬 Enviar Comprovante via WhatsApp", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = {
                                        step = 1
                                        createdAppointment = null
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Fazer Outro Agendamento")
                                }
                            }
                        }
                    }
                }
            }

            ClientPortalTab.MEUS_AGENDAMENTOS -> {
                // Consultar, Reagendar e Cancelar agendamentos do cliente
                Column(modifier = Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = clientFilterPhone,
                        onValueChange = { clientFilterPhone = it },
                        label = { Text("Filtrar por seu WhatsApp ou Nome") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (myAppointments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(44.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Nenhum agendamento encontrado", fontWeight = FontWeight.Bold)
                                Text("Digite seu telefone acima ou faça um novo agendamento.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(myAppointments) { appointment ->
                                val isCancelled = appointment.status == "CANCELADO"

                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = appointment.serviceName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                textDecoration = if (isCancelled) TextDecoration.LineThrough else TextDecoration.None
                                            )
                                            Surface(
                                                color = when (appointment.status) {
                                                    "CONFIRMADO" -> Color(0xFFE8F5E9)
                                                    "CANCELADO" -> Color(0xFFFFEBEE)
                                                    "CONCLUIDO" -> Color(0xFFE3F2FD)
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = appointment.status,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = when (appointment.status) {
                                                        "CONFIRMADO" -> Color(0xFF2E7D32)
                                                        "CANCELADO" -> Color(0xFFC62828)
                                                        "CONCLUIDO" -> Color(0xFF1565C0)
                                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                    },
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("👤 Profissional: ${appointment.professionalName}", fontSize = 13.sp)
                                        Text("📅 Data & Horário: ${appointment.dateStr} às ${appointment.timeStr}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text("💰 Valor: ${currencyFormat.format(appointment.price)}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)

                                        if (!isCancelled) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        appointmentToReschedule = appointment
                                                    },
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Reagendar", fontSize = 12.sp)
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        onCancelAppointment(appointment.id)
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color(0xFFD32F2F)
                                                    ),
                                                    modifier = Modifier.weight(1f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Cancelar", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ClientPortalTab.CONSULTORA_IA -> {
                // Assistente / Consultora Capilar Virtual
                var questionText by remember { mutableStateOf("") }
                var chatAnswer by remember { mutableStateOf<String?>(null) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dúvidas sobre o tratamento ideal? Pergunte para a nossa consultora capilar!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Qual o melhor tratamento pós-mechas?",
                            "Diferença de Botox e Progressiva",
                            "Cronograma Capilar para fios finos",
                            "Como manter a cor sem desbotar?"
                        ).forEach { suggestion ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    questionText = suggestion
                                    chatAnswer = when (suggestion) {
                                        "Qual o melhor tratamento pós-mechas?" ->
                                            "✨ Para cabelos com mechas recentes, recomendamos a Reconstrução Capilar com reposição de massa lipídica e queratina nos primeiros 15 dias, seguida de Nutrição profunda para selar as cutículas e devolver o brilho."
                                        "Diferença de Botox e Progressiva" ->
                                            "✨ O Botox Capilar tem foco em hidratação, alinhamento suave e redução de frizz sem alterar permanentemente a estrutura dos fios. Já a Progressiva Orgânica promove alisamento duradouro e brilho espelhado."
                                        else ->
                                            "✨ No $salonName trabalhamos com diagnósticos personalizados com Vanira e Vanessa para encontrar a fórmula exata para a saúde dos seus fios!"
                                    }
                                }
                            ) {
                                Text(suggestion, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("Ex: Tenho cabelo oleoso na raiz e seco nas pontas...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (questionText.isNotBlank()) {
                                chatAnswer = "✨ Baseado no seu relato, recomendamos agendar um diagnóstico capilar com Vanira ou Vanessa acompanhado de um tratamento de Nutrição & Cronograma para equilibrar a oleosidade e recuperar as pontas!"
                            }
                        },
                        enabled = questionText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perguntar à Especialista")
                    }

                    if (chatAnswer != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("💇‍♀️ Resposta da Consultora:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(chatAnswer!!, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                    }
                }
            }

            ClientPortalTab.ENVIAR_WEB_APP -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF2E7D32)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🌐", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Enviar Web App para Cliente",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20)
                                            )
                                        )
                                        Text(
                                            text = "Totalmente interligado com a base de dados em tempo real",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF2E7D32),
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "O cliente recebe o Web App pelo WhatsApp e abre em qualquer celular (Android ou iPhone) para agendar em tempo real, sem erro de página não encontrada.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF1B5E20),
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )
                                )
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "1. Buscar Cliente Cadastrado (com WhatsApp):",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = shareClientSearchQuery,
                                        onValueChange = { shareClientSearchQuery = it },
                                        placeholder = { Text("Nome ou número do WhatsApp...", fontSize = 12.sp) },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        trailingIcon = {
                                            if (shareClientSearchQuery.isNotEmpty()) {
                                                IconButton(onClick = { shareClientSearchQuery = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Limpar")
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .testTag("search_client_for_webapp_input"),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    // Botão colorido para buscar o nome do cliente com WhatsApp
                                    Button(
                                        onClick = {
                                            if (shareClientSearchQuery.isNotBlank()) {
                                                val match = clients.firstOrNull {
                                                    it.name.contains(shareClientSearchQuery, ignoreCase = true) ||
                                                    it.phone.contains(shareClientSearchQuery)
                                                }
                                                if (match != null) {
                                                    selectedShareClient = match
                                                    shareCustomName = match.name
                                                    shareCustomPhone = match.phone
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF673AB7), // Vibrant Deep Purple
                                            contentColor = Color.White
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .height(52.dp)
                                            .testTag("btn_search_client_webapp")
                                    ) {
                                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buscar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // Apresentação da cliente selecionada com WhatsApp
                                if (selectedShareClient != null) {
                                    val client = selectedShareClient!!
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5)),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAB47BC))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF8E24AA)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = client.name.firstOrNull()?.toString() ?: "C",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 16.sp
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = "Cliente Localizada",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF6A1B9A),
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Surface(
                                                            color = Color(0xFF2E7D32),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(
                                                                text = "Base de Dados",
                                                                color = Color.White,
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = client.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = Color(0xFF4A148C)
                                                    )
                                                    Text(
                                                        text = "📱 WhatsApp: ${client.phone}",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF1B5E20),
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }

                                            IconButton(onClick = {
                                                selectedShareClient = null
                                                shareCustomName = ""
                                                shareCustomPhone = ""
                                            }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Limpar seleção", tint = Color(0xFF8E24AA))
                                            }
                                        }
                                    }
                                }

                                val matchingClients = if (shareClientSearchQuery.isBlank()) {
                                    clients.take(4)
                                } else {
                                    clients.filter {
                                        it.name.contains(shareClientSearchQuery, ignoreCase = true) ||
                                        it.phone.contains(shareClientSearchQuery)
                                    }
                                }

                                if (matchingClients.isNotEmpty() && selectedShareClient == null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (shareClientSearchQuery.isBlank()) "Clientes na base de dados:" else "Resultados encontrados (${matchingClients.size}):",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        matchingClients.forEach { c ->
                                            val isSelected = selectedShareClient?.id == c.id
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                                        else MaterialTheme.colorScheme.surface
                                                    )
                                                    .clickable {
                                                        selectedShareClient = c
                                                        shareCustomName = c.name
                                                        shareCustomPhone = c.phone
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = c.name,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = "📱 WhatsApp: ${c.phone}",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.outline
                                                    )
                                                }
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                } else {
                                                    FilledTonalButton(
                                                        onClick = {
                                                            selectedShareClient = c
                                                            shareCustomName = c.name
                                                            shareCustomPhone = c.phone
                                                        },
                                                        shape = RoundedCornerShape(6.dp),
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("Selecionar", fontSize = 10.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Ou informe manualmente o contato:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = shareCustomName,
                                        onValueChange = {
                                            shareCustomName = it
                                            if (selectedShareClient?.name != it) selectedShareClient = null
                                        },
                                        placeholder = { Text("Nome da Cliente") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    OutlinedTextField(
                                        value = shareCustomPhone,
                                        onValueChange = {
                                            shareCustomPhone = it
                                            if (selectedShareClient?.phone != it) selectedShareClient = null
                                        },
                                        placeholder = { Text("WhatsApp (DDD)") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Conexão com a Base de Dados em Tempo Real:",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("✂️ ${services.size} Serviços Ativos", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("💇‍♀️ ${professionals.size} Profissionais", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("📅 Agenda Sincronizada", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    item {
                        val clientToSendName = shareCustomName.ifBlank { selectedShareClient?.name ?: "" }
                        val clientToSendPhone = shareCustomPhone.ifBlank { selectedShareClient?.phone ?: "" }
                        val canSend = clientToSendPhone.isNotBlank()

                        Button(
                            onClick = {
                                onShareWebApp?.invoke(selectedShareClient, clientToSendPhone, clientToSendName)
                            },
                            enabled = canSend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("send_webapp_client_whatsapp_btn")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (clientToSendName.isNotBlank()) "Enviar Web App para $clientToSendName" else "Enviar Web App via WhatsApp",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    onOpenWebAppInBrowser?.invoke(selectedShareClient)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_webapp_in_browser_btn")
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Testar no Navegador", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            if (onSharePortalClick != null) {
                                OutlinedButton(
                                    onClick = onSharePortalClick,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("open_share_dialog_btn")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Opções de Link", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            ClientPortalTab.MEUS_DADOS -> {
                // Perfil e Preferências Capilares
                var nameInput by remember(clientName) { mutableStateOf(clientName) }
                var phoneInput by remember(clientPhone) { mutableStateOf(clientPhone) }
                var hairPrefInput by remember(clientNotes) { mutableStateOf(clientNotes) }
                var savedNotice by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Seu Perfil no $salonName:",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Essas informações ajudam a equipe a preparar os produtos perfeitos para o seu atendimento.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Nome Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("WhatsApp") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = hairPrefInput,
                        onValueChange = { hairPrefInput = it },
                        label = { Text("Preferências Capilares e Cuidados") },
                        placeholder = { Text("Ex: Mechas loiro mel, raiz esfumada, evita formol, couro sensível...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            clientName = nameInput
                            clientPhone = phoneInput
                            clientNotes = hairPrefInput
                            if (activeClient != null && onSaveClientProfile != null) {
                                onSaveClientProfile(
                                    activeClient.copy(
                                        name = nameInput,
                                        phone = phoneInput,
                                        hairPreferences = hairPrefInput
                                    )
                                )
                            }
                            savedNotice = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Salvar Meus Dados")
                    }

                    if (savedNotice) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Dados salvos com sucesso na base de dados!",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Conflict Alert Dialog
    if (showConflictAlert) {
        AlertDialog(
            onDismissRequest = { showConflictAlert = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Horário Indisponível", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Este horário já foi preenchido ou bloqueado para este profissional. Por favor, escolha outro horário disponível marcado em verde.")
            },
            confirmButton = {
                Button(onClick = { showConflictAlert = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    // Reschedule Dialog
    if (appointmentToReschedule != null) {
        val app = appointmentToReschedule!!
        var reschedDate by remember { mutableStateOf(todayDate) }
        var reschedTime by remember { mutableStateOf("14:00") }
        val reschedOccupied = occupiedTimesProvider(app.professionalId, reschedDate)

        AlertDialog(
            onDismissRequest = { appointmentToReschedule = null },
            title = { Text("Reagendar Horário", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Serviço: ${app.serviceName}")
                    Text("Profissional: ${app.professionalName}")

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Novo Dia:", fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Hoje" to todayDate,
                            "Amanhã" to "2026-09-28",
                            "Terça" to "2026-09-29",
                            "Quarta" to "2026-09-30",
                            "Quinta" to "2026-10-01"
                        ).forEach { (lbl, d) ->
                            FilterChip(
                                selected = reschedDate == d,
                                onClick = { reschedDate = d },
                                label = { Text(lbl, fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Novo Horário:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        times.forEach { t ->
                            val occ = reschedOccupied.contains(t)
                            FilterChip(
                                selected = reschedTime == t && !occ,
                                onClick = { if (!occ) reschedTime = t },
                                enabled = !occ,
                                label = { Text(t, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRescheduleAppointment(app, reschedDate, reschedTime)
                        appointmentToReschedule = null
                    },
                    enabled = !reschedOccupied.contains(reschedTime)
                ) {
                    Text("Confirmar Novo Horário")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { appointmentToReschedule = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    title: String,
    active: Boolean,
    completed: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(
                    when {
                        completed -> Color(0xFF2E7D32)
                        active -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (completed) "✓" else stepNumber.toString(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (completed || active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        )
    }
}
