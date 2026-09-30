package com.example.data

import android.content.Context
import com.example.data.backup.BackupInfo
import com.example.data.backup.BackupResult
import com.example.data.backup.DatabaseBackupManager
import com.example.data.backup.RestoreResult
import com.example.data.dao.SalonDao
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Product
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.ScheduleBlock
import kotlinx.coroutines.flow.Flow

class SalonRepository(
    private val dao: SalonDao,
    private val context: Context? = null
) {
    private val prefs = context?.getSharedPreferences("salon_settings_prefs", Context.MODE_PRIVATE)

    fun getSalonName(): String {
        return prefs?.getString("salon_name", "Vanira e Vanessa Salão Especializado")
            ?: "Vanira e Vanessa Salão Especializado"
    }

    fun setSalonName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            prefs?.edit()?.putString("salon_name", trimmed)?.apply()
        }
    }

    // Professionals
    val allProfessionals: Flow<List<Professional>> = dao.getAllProfessionals()
    val activeProfessionals: Flow<List<Professional>> = dao.getActiveProfessionals()
    suspend fun insertProfessional(prof: Professional): Long = dao.insertProfessional(prof)
    suspend fun updateProfessional(prof: Professional) = dao.updateProfessional(prof)
    suspend fun deleteProfessional(prof: Professional) = dao.deleteProfessional(prof)

    // Clients
    val allClients: Flow<List<Client>> = dao.getAllClients()
    fun searchClients(query: String): Flow<List<Client>> = dao.searchClients(query)
    suspend fun getClientById(id: Long): Client? = dao.getClientById(id)
    fun getInactiveClients(cutoffTimestamp: Long): Flow<List<Client>> = dao.getInactiveClients(cutoffTimestamp)
    suspend fun insertClient(client: Client): Long = dao.insertClient(client)
    suspend fun updateClient(client: Client) = dao.updateClient(client)
    suspend fun deleteClient(client: Client) = dao.deleteClient(client)

    // Services
    val allServices: Flow<List<SalonService>> = dao.getAllServices()
    suspend fun getServiceById(id: Long): SalonService? = dao.getServiceById(id)
    suspend fun insertService(service: SalonService): Long = dao.insertService(service)
    suspend fun updateService(service: SalonService) = dao.updateService(service)
    suspend fun deleteService(service: SalonService) = dao.deleteService(service)

    // Appointments
    val allAppointments: Flow<List<Appointment>> = dao.getAllAppointments()
    fun getAppointmentsByDate(dateStr: String): Flow<List<Appointment>> = dao.getAppointmentsByDate(dateStr)
    fun getAppointmentsForClient(name: String, id: Long?): Flow<List<Appointment>> = dao.getAppointmentsForClient(name, id)
    fun getAppointmentsByProfessional(profId: Long, dateStr: String): Flow<List<Appointment>> =
        dao.getAppointmentsByProfessionalAndDate(profId, dateStr)

    suspend fun insertAppointment(app: Appointment): Long = dao.insertAppointment(app)
    suspend fun updateAppointment(app: Appointment) = dao.updateAppointment(app)
    suspend fun updateAppointmentStatus(id: Long, status: String) = dao.updateAppointmentStatus(id, status)
    suspend fun deleteAppointment(id: Long) = dao.deleteAppointmentById(id)

    // Transactions / Financeiro
    val allTransactions: Flow<List<PaymentTransaction>> = dao.getAllTransactions()
    val pendingTransactions: Flow<List<PaymentTransaction>> = dao.getPendingTransactions()
    fun getTransactionsByDate(dateStr: String): Flow<List<PaymentTransaction>> = dao.getTransactionsByDate(dateStr)
    suspend fun insertTransaction(tx: PaymentTransaction): Long = dao.insertTransaction(tx)
    suspend fun markTransactionPaid(id: Long, method: String, paidAt: Long) = dao.markTransactionPaid(id, method, paidAt)
    suspend fun deleteTransaction(id: Long) = dao.deleteTransactionById(id)

    // Schedule Blocks
    val allScheduleBlocks: Flow<List<ScheduleBlock>> = dao.getAllScheduleBlocks()
    fun getBlocksByDate(dateStr: String): Flow<List<ScheduleBlock>> = dao.getBlocksByDate(dateStr)
    suspend fun insertBlock(block: ScheduleBlock): Long = dao.insertBlock(block)
    suspend fun deleteBlock(block: ScheduleBlock) = dao.deleteBlock(block)
    suspend fun deleteBlockById(id: Long) = dao.deleteBlockById(id)

    // Products / Estoque
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)
    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun updateProductStock(id: Long, qty: Int) = dao.updateProductStock(id, qty)

    // Client Portal & Token queries
    suspend fun getClientByTokenOrPhone(token: String): Client? = dao.getClientByTokenOrPhone(token)
    fun getAppointmentsByClientPhone(phone: String): Flow<List<Appointment>> = dao.getAppointmentsByClientPhone(phone)

    fun getClientPortalUrl(): String {
        return prefs?.getString("client_portal_url", "https://ais-pre-jm76sl636hxzzikrdhzmmk-854340488351.us-east1.run.app")
            ?: "https://ais-pre-jm76sl636hxzzikrdhzmmk-854340488351.us-east1.run.app"
    }

    fun setClientPortalUrl(url: String) {
        val trimmed = url.trim()
        if (trimmed.isNotBlank()) {
            prefs?.edit()?.putString("client_portal_url", trimmed)?.apply()
        }
    }

    // Supabase Cloud Configuration
    fun getSupabaseUrl(): String = prefs?.getString("supabase_url", "https://xyzcompany.supabase.co") ?: "https://xyzcompany.supabase.co"
    fun setSupabaseUrl(url: String) = prefs?.edit()?.putString("supabase_url", url.trim())?.apply()

    fun getSupabaseKey(): String = prefs?.getString("supabase_key", "anon-key-placeholder") ?: "anon-key-placeholder"
    fun setSupabaseKey(key: String) = prefs?.edit()?.putString("supabase_key", key.trim())?.apply()

    fun isSupabaseRealtimeEnabled(): Boolean = prefs?.getBoolean("supabase_realtime_active", true) ?: true
    fun setSupabaseRealtimeEnabled(enabled: Boolean) = prefs?.edit()?.putBoolean("supabase_realtime_active", enabled)?.apply()

    fun setAllowBlankDb(allow: Boolean) {
        prefs?.edit()?.putBoolean("allow_blank_db", allow)?.apply()
    }

    suspend fun clearAppointmentsAndFinancialOnly() {
        dao.clearAllAppointments()
        dao.clearAllTransactions()
        dao.clearAllScheduleBlocks()
    }

    suspend fun clearAllDataBlank(resetSalonName: Boolean = false) {
        setAllowBlankDb(true)
        dao.clearAllAppointments()
        dao.clearAllTransactions()
        dao.clearAllScheduleBlocks()
        dao.clearAllClients()
        dao.clearAllProducts()
        dao.clearAllServices()
        dao.clearAllProfessionals()
        if (resetSalonName) {
            setSalonName("Vanira e Vanessa Salão Especializado")
        }
    }

    suspend fun restoreDefaultData(resetSalonName: Boolean = false) {
        setAllowBlankDb(false)
        dao.clearAllAppointments()
        dao.clearAllTransactions()
        dao.clearAllScheduleBlocks()
        dao.clearAllClients()
        dao.clearAllProducts()
        dao.clearAllServices()
        dao.clearAllProfessionals()
        populateInitialData(dao)
        if (resetSalonName) {
            setSalonName("Vanira e Vanessa Salão Especializado")
        }
    }

    suspend fun ensureSeeded() {
        try {
            val allowBlank = prefs?.getBoolean("allow_blank_db", false) ?: false
            if (!allowBlank && (dao.getServicesCount() == 0 || dao.getProductsCount() == 0)) {
                populateInitialData(dao)
            }
        } catch (e: Exception) {
            android.util.Log.e("SalonRepository", "Error in ensureSeeded", e)
        }
    }

    // --- Backup & Restauração ---
    val backupManager = context?.let { DatabaseBackupManager(dao, it) }
    private val webAppManager = context?.let { com.example.data.webapp.WebAppManager(it) }

    fun isClientDevice(): Boolean = prefs?.getBoolean("is_client_device_locked", false) ?: false
    fun setClientDevice(isClient: Boolean) {
        prefs?.edit()?.putBoolean("is_client_device_locked", isClient)?.apply()
    }

    fun getAdminPin(): String = prefs?.getString("admin_security_pin", "1234") ?: "1234"
    fun setAdminPin(pin: String) {
        val trimmed = pin.trim()
        if (trimmed.isNotBlank()) {
            prefs?.edit()?.putString("admin_security_pin", trimmed)?.apply()
        }
    }

    suspend fun generateWebAppHtml(targetClient: Client? = null): String? {
        val manager = webAppManager ?: return null
        val services = dao.getServicesList()
        val professionals = dao.getProfessionalsList()
        return manager.generateWebAppHtml(
            salonName = getSalonName(),
            services = services,
            professionals = professionals,
            targetClient = targetClient
        )
    }

    suspend fun shareWebApp(targetClient: Client? = null, clientPhone: String = "", clientName: String = "") {
        val manager = webAppManager ?: return
        val html = generateWebAppHtml(targetClient) ?: return
        val file = manager.saveWebAppFile(html)
        manager.shareWebAppFile(file, getSalonName(), clientPhone, clientName)
    }

    suspend fun openWebAppInBrowser(targetClient: Client? = null) {
        val manager = webAppManager ?: return
        val html = generateWebAppHtml(targetClient) ?: return
        val file = manager.saveWebAppFile(html)
        manager.openWebAppInBrowser(file)
    }

    suspend fun createLocalBackup(): BackupResult? {
        return backupManager?.createBackup(getSalonName())
    }

    suspend fun restoreFromBackupJson(jsonString: String): RestoreResult? {
        val result = backupManager?.restoreFromBackupJson(jsonString)
        if (result?.success == true) {
            setAllowBlankDb(false)
        }
        return result
    }

    fun getLastBackupInfo(): BackupInfo? = backupManager?.getLastBackupInfo()

    fun shareBackup(jsonContent: String) {
        backupManager?.shareBackup(jsonContent, getSalonName())
    }

    fun isAutoBackupEnabled(): Boolean = backupManager?.isAutoBackupEnabled() ?: true

    fun setAutoBackupEnabled(enabled: Boolean) {
        backupManager?.setAutoBackupEnabled(enabled)
    }
}
