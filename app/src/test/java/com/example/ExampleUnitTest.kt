package com.example

import com.example.ai.GeminiService
import com.example.data.model.Appointment
import com.example.data.model.AppointmentWithDetails
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.executesService
import com.example.data.model.getExecutedServiceIds
import com.example.data.model.isExecutedBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSalonAnalyticsLocalEngine() {
        val geminiService = GeminiService()

        val clients = listOf(
            Client(
                id = 1,
                name = "Camila Fagundes",
                phone = "11972348899",
                hairPreferences = "Loiro pérola",
                lastVisitTimestamp = System.currentTimeMillis() - (70L * 24 * 60 * 60 * 1000) // 70 dias atrás
            )
        )

        val services = listOf(
            SalonService(id = 1, name = "Corte Feminino", category = "Cabelo", price = 80.0, durationMinutes = 45)
        )

        val appointments = listOf(
            Appointment(
                id = 1,
                clientName = "Ana",
                clientPhone = "11999999999",
                serviceId = 1,
                serviceName = "Corte Feminino",
                professionalId = 1,
                professionalName = "Carla",
                dateStr = "2026-09-27",
                timeStr = "09:00",
                durationMinutes = 45,
                price = 80.0,
                status = "CONFIRMADO"
            )
        )

        val transactions = listOf(
            PaymentTransaction(
                id = 1,
                clientName = "Ana",
                serviceName = "Corte Feminino",
                amount = 80.0,
                dateStr = "2026-09-27",
                paymentMethod = "PIX",
                status = "PAGO"
            ),
            PaymentTransaction(
                id = 2,
                clientName = "Fernanda",
                serviceName = "Mechas",
                amount = 290.0,
                dateStr = "2026-09-27",
                paymentMethod = "PENDENTE",
                status = "PENDENTE"
            )
        )

        // 1. Testa consulta de faturamento
        val revenueAnswer = geminiService.analyzeLocally("Quanto faturei este mês?", clients, services, appointments, transactions)
        assertTrue(revenueAnswer.contains("80,00"))

        // 2. Testa consulta de clientes > 60 dias inativos
        val inactiveAnswer = geminiService.analyzeLocally("Quais clientes estão há mais de 60 dias sem voltar?", clients, services, appointments, transactions)
        assertTrue(inactiveAnswer.contains("Camila Fagundes"))

        // 3. Testa consulta de contas a receber
        val pendingAnswer = geminiService.analyzeLocally("Quanto tenho para receber?", clients, services, appointments, transactions)
        assertTrue(pendingAnswer.contains("290,00"))
    }

    @Test
    fun testServiceManagement() {
        val initialServices = mutableListOf(
            SalonService(id = 1, name = "Corte Feminino", category = "Cabelo", price = 85.0, durationMinutes = 45, description = "Corte visagista"),
            SalonService(id = 2, name = "Manicure", category = "Unhas", price = 35.0, durationMinutes = 40, description = "Cutilagem e esmaltação")
        )

        // Adicionar novo serviço
        val newService = SalonService(id = 3, name = "Botox Capilar", category = "Tratamentos", price = 150.0, durationMinutes = 60, description = "Alinhamento e brilho")
        initialServices.add(newService)
        assertEquals(3, initialServices.size)
        assertTrue(initialServices.any { it.name == "Botox Capilar" })

        // Editar serviço existente
        val indexToEdit = initialServices.indexOfFirst { it.id == 1L }
        val updatedService = initialServices[indexToEdit].copy(price = 95.0, description = "Corte visagista com lavagem especial")
        initialServices[indexToEdit] = updatedService
        assertEquals(95.0, initialServices.first { it.id == 1L }.price, 0.01)
        assertEquals("Corte visagista com lavagem especial", initialServices.first { it.id == 1L }.description)

        // Remover serviço
        initialServices.removeAll { it.id == 2L }
        assertEquals(2, initialServices.size)
        assertTrue(initialServices.none { it.id == 2L })
    }

    @Test
    fun testRoomEntitiesAndRelationships() {
        val prof = com.example.data.model.Professional(
            id = 1,
            name = "Carla Mendes",
            role = "Colorista",
            avatarEmoji = "💇‍♀️",
            phone = "11999998888",
            serviceIdsCsv = "1,2,3"
        )
        val service = SalonService(
            id = 1,
            name = "Mechas",
            category = "Química & Cor",
            price = 290.0,
            durationMinutes = 120,
            professionalIdsCsv = "1"
        )
        val client = Client(
            id = 10,
            name = "Luciana",
            phone = "11988887777",
            hairPreferences = "Loiro platinado"
        )
        val appointment = Appointment(
            id = 100,
            clientName = client.name,
            clientPhone = client.phone,
            clientId = client.id,
            serviceId = service.id,
            serviceName = service.name,
            professionalId = prof.id,
            professionalName = prof.name,
            dateStr = "2026-09-27",
            timeStr = "14:00",
            durationMinutes = service.durationMinutes,
            price = service.price,
            status = "CONFIRMADO"
        )

        // Test helper methods
        assertTrue(prof.executesService(1L))
        assertTrue(service.isExecutedBy(1L))
        assertEquals(listOf(1L, 2L, 3L), prof.getExecutedServiceIds())

        // Test AppointmentWithDetails model
        val details = AppointmentWithDetails(
            appointment = appointment,
            client = client,
            professional = prof,
            service = service
        )
        assertEquals("Carla Mendes", details.professional?.name)
        assertEquals("Luciana", details.client?.name)
        assertEquals("Mechas", details.service?.name)
        assertEquals("CONFIRMADO", details.appointment.status)
    }
}
