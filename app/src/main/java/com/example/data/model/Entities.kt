package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "professionals",
    indices = [
        Index(value = ["name"]),
        Index(value = ["active"])
    ]
)
data class Professional(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val avatarEmoji: String = "💇‍♀️",
    val phone: String = "",
    val active: Boolean = true,
    val rating: Double = 4.9,
    val serviceIdsCsv: String = "all" // IDs dos serviços separados por vírgula ou "all"
)

fun Professional.executesService(serviceId: Long): Boolean {
    if (serviceIdsCsv == "all" || serviceIdsCsv.isBlank()) return true
    val ids = serviceIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
    return ids.contains(serviceId)
}

fun Professional.getExecutedServiceIds(): List<Long> {
    if (serviceIdsCsv == "all" || serviceIdsCsv.isBlank()) return emptyList()
    return serviceIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

fun SalonService.isExecutedBy(professionalId: Long): Boolean {
    if (professionalIdsCsv == "all" || professionalIdsCsv.isBlank()) return true
    val ids = professionalIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
    return ids.contains(professionalId)
}

fun SalonService.getAssignedProfessionalIds(): List<Long> {
    if (professionalIdsCsv == "all" || professionalIdsCsv.isBlank()) return emptyList()
    return professionalIdsCsv.split(",").mapNotNull { it.trim().toLongOrNull() }
}

@Entity(
    tableName = "clients",
    indices = [
        Index(value = ["phone"]),
        Index(value = ["name"]),
        Index(value = ["lastVisitTimestamp"])
    ]
)
data class Client(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val birthDate: String = "",
    val address: String = "",
    val hairPreferences: String = "",
    val notes: String = "",
    val token: String = "", // Token individual exclusivo para acesso ao portal do cliente
    val registeredAt: Long = System.currentTimeMillis(),
    val lastVisitTimestamp: Long = System.currentTimeMillis()
) {
    fun getPortalToken(): String {
        if (token.isNotBlank()) return token
        val digits = phone.filter { it.isDigit() }
        return if (digits.isNotBlank()) "cli_$digits" else "cli_$id"
    }
}

@Entity(
    tableName = "services",
    indices = [
        Index(value = ["category"]),
        Index(value = ["name"])
    ]
)
data class SalonService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // "Cabelo", "Química & Cor", "Unhas", "Tratamentos"
    val price: Double,
    val durationMinutes: Int,
    val description: String = "",
    val iconName: String = "content_cut",
    val professionalIdsCsv: String = "all"
)

@Entity(
    tableName = "appointments",
    indices = [
        Index(value = ["dateStr"]),
        Index(value = ["professionalId"]),
        Index(value = ["clientId"]),
        Index(value = ["status"])
    ]
)
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val clientPhone: String,
    val clientId: Long? = null,
    val serviceId: Long,
    val serviceName: String,
    val professionalId: Long,
    val professionalName: String,
    val dateStr: String, // YYYY-MM-DD
    val timeStr: String, // HH:mm (ex: "09:00", "10:30")
    val durationMinutes: Int,
    val price: Double,
    val status: String = "CONFIRMADO", // "CONFIRMADO", "AGUARDANDO", "CONCLUIDO", "CANCELADO", "BLOQUEIO"
    val notes: String = "",
    val googleCalendarEventId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AppointmentWithDetails(
    @Embedded val appointment: Appointment,
    @Relation(
        parentColumn = "clientId",
        entityColumn = "id"
    )
    val client: Client? = null,
    @Relation(
        parentColumn = "professionalId",
        entityColumn = "id"
    )
    val professional: Professional? = null,
    @Relation(
        parentColumn = "serviceId",
        entityColumn = "id"
    )
    val service: SalonService? = null
)

@Entity(tableName = "transactions")
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appointmentId: Long? = null,
    val clientName: String,
    val serviceName: String,
    val amount: Double,
    val dateStr: String, // YYYY-MM-DD
    val paymentMethod: String = "PIX", // "PIX", "CARTAO_CREDITO", "CARTAO_DEBITO", "DINHEIRO", "PENDENTE"
    val status: String = "PAGO", // "PAGO", "PENDENTE"
    val dueDate: String = "",
    val paidAt: Long? = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "schedule_blocks")
data class ScheduleBlock(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val professionalId: Long,
    val dateStr: String,
    val startTime: String,
    val endTime: String,
    val reason: String
)

@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val brand: String = "",
    val category: String = "Home Care", // "Home Care", "Tratamento", "Coloração", "Finalizadores", "Acessórios"
    val quantityInStock: Int = 0,
    val minStockAlert: Int = 3,
    val costPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val barcode: String = "",
    val description: String = ""
)

