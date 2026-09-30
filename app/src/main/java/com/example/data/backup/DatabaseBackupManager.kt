package com.example.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.dao.SalonDao
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Product
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.ScheduleBlock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupInfo(
    val timestamp: Long,
    val formattedDate: String,
    val filePath: String,
    val fileSizeKb: Long,
    val clientsCount: Int,
    val appointmentsCount: Int,
    val servicesCount: Int,
    val professionalsCount: Int,
    val productsCount: Int,
    val transactionsCount: Int
)

data class BackupResult(
    val success: Boolean,
    val message: String,
    val backupInfo: BackupInfo? = null,
    val jsonContent: String = ""
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val restoredClients: Int = 0,
    val restoredAppointments: Int = 0,
    val restoredServices: Int = 0,
    val restoredProfessionals: Int = 0,
    val restoredProducts: Int = 0
)

class DatabaseBackupManager(
    private val dao: SalonDao,
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("salon_backup_prefs", Context.MODE_PRIVATE)

    fun isAutoBackupEnabled(): Boolean {
        return prefs.getBoolean("auto_backup_enabled", true)
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
    }

    fun getLastBackupInfo(): BackupInfo? {
        val timestamp = prefs.getLong("last_backup_ts", 0L)
        if (timestamp == 0L) return null
        return BackupInfo(
            timestamp = timestamp,
            formattedDate = prefs.getString("last_backup_date", "") ?: "",
            filePath = prefs.getString("last_backup_path", "") ?: "",
            fileSizeKb = prefs.getLong("last_backup_size_kb", 0L),
            clientsCount = prefs.getInt("last_backup_clients", 0),
            appointmentsCount = prefs.getInt("last_backup_appointments", 0),
            servicesCount = prefs.getInt("last_backup_services", 0),
            professionalsCount = prefs.getInt("last_backup_professionals", 0),
            productsCount = prefs.getInt("last_backup_products", 0),
            transactionsCount = prefs.getInt("last_backup_transactions", 0)
        )
    }

    suspend fun createBackup(salonName: String = "Vanira e Vanessa Salão Especializado"): BackupResult {
        return try {
            val professionals = dao.getProfessionalsList()
            val clients = dao.getClientsList()
            val services = dao.getServicesList()
            val appointments = dao.getAppointmentsList()
            val transactions = dao.getTransactionsList()
            val blocks = dao.getScheduleBlocksList()
            val products = dao.getProductsList()

            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale("pt", "BR"))
            val formattedDate = dateFormat.format(Date(now))

            val rootJson = JSONObject().apply {
                put("app", salonName)
                put("schema_version", 1)
                put("timestamp", now)
                put("date_formatted", formattedDate)

                val summary = JSONObject().apply {
                    put("professionals", professionals.size)
                    put("clients", clients.size)
                    put("services", services.size)
                    put("appointments", appointments.size)
                    put("transactions", transactions.size)
                    put("schedule_blocks", blocks.size)
                    put("products", products.size)
                }
                put("summary", summary)

                // 1. Professionals
                val profArray = JSONArray()
                professionals.forEach { p ->
                    profArray.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("role", p.role)
                        put("phone", p.phone)
                        put("avatarEmoji", p.avatarEmoji)
                        put("rating", p.rating)
                        put("serviceIdsCsv", p.serviceIdsCsv)
                        put("active", p.active)
                    })
                }
                put("professionals", profArray)

                // 2. Clients
                val clientsArray = JSONArray()
                clients.forEach { c ->
                    clientsArray.put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("phone", c.phone)
                        put("birthDate", c.birthDate)
                        put("address", c.address)
                        put("hairPreferences", c.hairPreferences)
                        put("notes", c.notes)
                        put("lastVisitTimestamp", c.lastVisitTimestamp)
                        put("token", c.token)
                    })
                }
                put("clients", clientsArray)

                // 3. Services
                val servicesArray = JSONArray()
                services.forEach { s ->
                    servicesArray.put(JSONObject().apply {
                        put("id", s.id)
                        put("name", s.name)
                        put("category", s.category)
                        put("price", s.price)
                        put("durationMinutes", s.durationMinutes)
                        put("description", s.description)
                        put("iconName", s.iconName)
                        put("professionalIdsCsv", s.professionalIdsCsv)
                    })
                }
                put("services", servicesArray)

                // 4. Appointments
                val appointmentsArray = JSONArray()
                appointments.forEach { a ->
                    appointmentsArray.put(JSONObject().apply {
                        put("id", a.id)
                        put("clientName", a.clientName)
                        put("clientPhone", a.clientPhone)
                        put("clientId", a.clientId ?: JSONObject.NULL)
                        put("serviceId", a.serviceId)
                        put("serviceName", a.serviceName)
                        put("professionalId", a.professionalId)
                        put("professionalName", a.professionalName)
                        put("dateStr", a.dateStr)
                        put("timeStr", a.timeStr)
                        put("durationMinutes", a.durationMinutes)
                        put("price", a.price)
                        put("status", a.status)
                        put("notes", a.notes)
                        put("createdAt", a.createdAt)
                    })
                }
                put("appointments", appointmentsArray)

                // 5. Transactions
                val transactionsArray = JSONArray()
                transactions.forEach { t ->
                    transactionsArray.put(JSONObject().apply {
                        put("id", t.id)
                        put("appointmentId", t.appointmentId ?: JSONObject.NULL)
                        put("clientName", t.clientName)
                        put("serviceName", t.serviceName)
                        put("amount", t.amount)
                        put("dateStr", t.dateStr)
                        put("paymentMethod", t.paymentMethod)
                        put("status", t.status)
                        put("dueDate", t.dueDate)
                        put("paidAt", t.paidAt ?: JSONObject.NULL)
                        put("notes", t.notes)
                    })
                }
                put("transactions", transactionsArray)

                // 6. Schedule Blocks
                val blocksArray = JSONArray()
                blocks.forEach { b ->
                    blocksArray.put(JSONObject().apply {
                        put("id", b.id)
                        put("professionalId", b.professionalId)
                        put("dateStr", b.dateStr)
                        put("startTime", b.startTime)
                        put("endTime", b.endTime)
                        put("reason", b.reason)
                    })
                }
                put("schedule_blocks", blocksArray)

                // 7. Products
                val productsArray = JSONArray()
                products.forEach { pr ->
                    productsArray.put(JSONObject().apply {
                        put("id", pr.id)
                        put("name", pr.name)
                        put("brand", pr.brand)
                        put("category", pr.category)
                        put("quantityInStock", pr.quantityInStock)
                        put("minStockAlert", pr.minStockAlert)
                        put("costPrice", pr.costPrice)
                        put("sellPrice", pr.sellPrice)
                        put("barcode", pr.barcode)
                        put("description", pr.description)
                    })
                }
                put("products", productsArray)
            }

            val jsonString = rootJson.toString(2)

            // Save to files directory
            val backupDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }
            val fileTs = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))
            val backupFile = File(backupDir, "backup_salao_${fileTs}.json")
            backupFile.writeText(jsonString)

            // Also keep latest pointer
            val latestFile = File(backupDir, "backup_salao_latest.json")
            latestFile.writeText(jsonString)

            val sizeKb = (backupFile.length() / 1024L).coerceAtLeast(1L)

            // Persist to SharedPreferences
            prefs.edit()
                .putLong("last_backup_ts", now)
                .putString("last_backup_date", formattedDate)
                .putString("last_backup_path", backupFile.absolutePath)
                .putLong("last_backup_size_kb", sizeKb)
                .putInt("last_backup_clients", clients.size)
                .putInt("last_backup_appointments", appointments.size)
                .putInt("last_backup_services", services.size)
                .putInt("last_backup_professionals", professionals.size)
                .putInt("last_backup_products", products.size)
                .putInt("last_backup_transactions", transactions.size)
                .apply()

            val info = BackupInfo(
                timestamp = now,
                formattedDate = formattedDate,
                filePath = backupFile.absolutePath,
                fileSizeKb = sizeKb,
                clientsCount = clients.size,
                appointmentsCount = appointments.size,
                servicesCount = services.size,
                professionalsCount = professionals.size,
                productsCount = products.size,
                transactionsCount = transactions.size
            )

            BackupResult(
                success = true,
                message = "Backup local realizado com sucesso!",
                backupInfo = info,
                jsonContent = jsonString
            )
        } catch (e: Exception) {
            android.util.Log.e("DatabaseBackupManager", "Error creating backup", e)
            BackupResult(
                success = false,
                message = "Falha ao criar backup: ${e.message}"
            )
        }
    }

    suspend fun restoreFromBackupJson(jsonString: String): RestoreResult {
        return try {
            val root = JSONObject(jsonString)

            // Extract collections
            val profArray = root.optJSONArray("professionals") ?: JSONArray()
            val clientArray = root.optJSONArray("clients") ?: JSONArray()
            val serviceArray = root.optJSONArray("services") ?: JSONArray()
            val appArray = root.optJSONArray("appointments") ?: JSONArray()
            val txArray = root.optJSONArray("transactions") ?: JSONArray()
            val blockArray = root.optJSONArray("schedule_blocks") ?: JSONArray()
            val prodArray = root.optJSONArray("products") ?: JSONArray()

            // Clear current data first
            dao.clearAllAppointments()
            dao.clearAllTransactions()
            dao.clearAllScheduleBlocks()
            dao.clearAllClients()
            dao.clearAllProducts()
            dao.clearAllServices()
            dao.clearAllProfessionals()

            // 1. Services
            val servicesList = mutableListOf<SalonService>()
            for (i in 0 until serviceArray.length()) {
                val o = serviceArray.getJSONObject(i)
                servicesList.add(
                    SalonService(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        category = o.optString("category", "Cabelo"),
                        price = o.optDouble("price", 0.0),
                        durationMinutes = o.optInt("durationMinutes", 30),
                        description = o.optString("description", ""),
                        iconName = o.optString("iconName", "content_cut"),
                        professionalIdsCsv = o.optString("professionalIdsCsv", "all")
                    )
                )
            }
            if (servicesList.isNotEmpty()) dao.insertServices(servicesList)

            // 2. Professionals
            val profList = mutableListOf<Professional>()
            for (i in 0 until profArray.length()) {
                val o = profArray.getJSONObject(i)
                profList.add(
                    Professional(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        role = o.optString("role", "Especialista"),
                        phone = o.optString("phone", ""),
                        avatarEmoji = o.optString("avatarEmoji", "💇‍♀️"),
                        rating = o.optDouble("rating", 5.0),
                        serviceIdsCsv = o.optString("serviceIdsCsv", "all"),
                        active = o.optBoolean("active", true)
                    )
                )
            }
            if (profList.isNotEmpty()) dao.insertProfessionals(profList)

            // 3. Clients
            val clientList = mutableListOf<Client>()
            for (i in 0 until clientArray.length()) {
                val o = clientArray.getJSONObject(i)
                clientList.add(
                    Client(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        phone = o.getString("phone"),
                        birthDate = o.optString("birthDate", ""),
                        address = o.optString("address", ""),
                        hairPreferences = o.optString("hairPreferences", ""),
                        notes = o.optString("notes", ""),
                        lastVisitTimestamp = o.optLong("lastVisitTimestamp", System.currentTimeMillis()),
                        token = o.optString("token", "")
                    )
                )
            }
            if (clientList.isNotEmpty()) dao.insertClients(clientList)

            // 4. Products
            val prodList = mutableListOf<Product>()
            for (i in 0 until prodArray.length()) {
                val o = prodArray.getJSONObject(i)
                prodList.add(
                    Product(
                        id = o.optLong("id", 0L),
                        name = o.getString("name"),
                        brand = o.optString("brand", ""),
                        category = o.optString("category", "Home Care"),
                        quantityInStock = o.optInt("quantityInStock", 0),
                        minStockAlert = o.optInt("minStockAlert", 3),
                        costPrice = o.optDouble("costPrice", 0.0),
                        sellPrice = o.optDouble("sellPrice", 0.0),
                        barcode = o.optString("barcode", ""),
                        description = o.optString("description", "")
                    )
                )
            }
            if (prodList.isNotEmpty()) dao.insertProducts(prodList)

            // 5. Appointments
            val appList = mutableListOf<Appointment>()
            for (i in 0 until appArray.length()) {
                val o = appArray.getJSONObject(i)
                val clientIdVal = if (o.isNull("clientId")) null else o.optLong("clientId")
                appList.add(
                    Appointment(
                        id = o.optLong("id", 0L),
                        clientName = o.getString("clientName"),
                        clientPhone = o.optString("clientPhone", ""),
                        clientId = clientIdVal,
                        serviceId = o.optLong("serviceId", 1L),
                        serviceName = o.optString("serviceName", "Serviço"),
                        professionalId = o.optLong("professionalId", 1L),
                        professionalName = o.optString("professionalName", "Profissional"),
                        dateStr = o.getString("dateStr"),
                        timeStr = o.getString("timeStr"),
                        durationMinutes = o.optInt("durationMinutes", 30),
                        price = o.optDouble("price", 0.0),
                        status = o.optString("status", "CONFIRMADO"),
                        notes = o.optString("notes", ""),
                        createdAt = o.optLong("createdAt", System.currentTimeMillis())
                    )
                )
            }
            if (appList.isNotEmpty()) dao.insertAppointments(appList)

            // 6. Transactions
            val txList = mutableListOf<PaymentTransaction>()
            for (i in 0 until txArray.length()) {
                val o = txArray.getJSONObject(i)
                val appId = if (o.isNull("appointmentId")) null else o.optLong("appointmentId")
                val paidAtVal = if (o.isNull("paidAt")) null else o.optLong("paidAt")
                txList.add(
                    PaymentTransaction(
                        id = o.optLong("id", 0L),
                        appointmentId = appId,
                        clientName = o.optString("clientName", ""),
                        serviceName = o.optString("serviceName", ""),
                        amount = o.optDouble("amount", 0.0),
                        dateStr = o.optString("dateStr", ""),
                        paymentMethod = o.optString("paymentMethod", "PIX"),
                        status = o.optString("status", "PAGO"),
                        dueDate = o.optString("dueDate", ""),
                        paidAt = paidAtVal,
                        notes = o.optString("notes", "")
                    )
                )
            }
            if (txList.isNotEmpty()) dao.insertTransactions(txList)

            // 7. Schedule Blocks
            val blockList = mutableListOf<ScheduleBlock>()
            for (i in 0 until blockArray.length()) {
                val o = blockArray.getJSONObject(i)
                blockList.add(
                    ScheduleBlock(
                        id = o.optLong("id", 0L),
                        professionalId = o.optLong("professionalId", 1L),
                        dateStr = o.getString("dateStr"),
                        startTime = o.getString("startTime"),
                        endTime = o.getString("endTime"),
                        reason = o.optString("reason", "Bloqueio")
                    )
                )
            }
            if (blockList.isNotEmpty()) dao.insertBlocks(blockList)

            RestoreResult(
                success = true,
                message = "Base de dados restaurada com sucesso!",
                restoredClients = clientList.size,
                restoredAppointments = appList.size,
                restoredServices = servicesList.size,
                restoredProfessionals = profList.size,
                restoredProducts = prodList.size
            )
        } catch (e: Exception) {
            android.util.Log.e("DatabaseBackupManager", "Error restoring backup", e)
            RestoreResult(
                success = false,
                message = "Erro ao restaurar arquivo de backup: ${e.message}"
            )
        }
    }

    fun shareBackup(jsonContent: String, salonName: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "Backup Base de Dados - $salonName")
                putExtra(Intent.EXTRA_TEXT, jsonContent)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(shareIntent, "Compartilhar Backup da Base de Dados").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            android.util.Log.e("DatabaseBackupManager", "Error sharing backup", e)
        }
    }
}
