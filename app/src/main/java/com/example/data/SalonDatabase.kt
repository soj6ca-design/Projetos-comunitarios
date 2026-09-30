package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppointmentDao
import com.example.data.dao.ClientDao
import com.example.data.dao.ProfessionalDao
import com.example.data.dao.SalonDao
import com.example.data.dao.ServiceDao
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Product
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.ScheduleBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        Professional::class,
        Client::class,
        SalonService::class,
        Appointment::class,
        PaymentTransaction::class,
        ScheduleBlock::class,
        Product::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SalonDatabase : RoomDatabase() {

    abstract fun salonDao(): SalonDao
    abstract fun professionalDao(): ProfessionalDao
    abstract fun clientDao(): ClientDao
    abstract fun serviceDao(): ServiceDao
    abstract fun appointmentDao(): AppointmentDao

    companion object {
        @Volatile
        private var INSTANCE: SalonDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): SalonDatabase {
            return INSTANCE ?: synchronized(this) {
                var instanceHolder: SalonDatabase? = null
                val callback = SalonDatabaseCallback(context.applicationContext, scope) {
                    instanceHolder ?: INSTANCE ?: getDatabase(context, scope)
                }
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SalonDatabase::class.java,
                    "studio_bella_db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(callback)
                    .build()
                instanceHolder = instance
                INSTANCE = instance

                // Safe proactive seed check on startup
                scope.launch(Dispatchers.IO) {
                    try {
                        val prefs = context.getSharedPreferences("salon_settings_prefs", Context.MODE_PRIVATE)
                        val allowBlank = prefs.getBoolean("allow_blank_db", false)
                        if (!allowBlank && instance.salonDao().getServicesCount() == 0) {
                            populateInitialData(instance.salonDao())
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SalonDatabase", "Error ensuring database is seeded", e)
                    }
                }

                instance
            }
        }
    }

    private class SalonDatabaseCallback(
        private val context: Context,
        private val scope: CoroutineScope,
        private val dbProvider: () -> SalonDatabase
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch(Dispatchers.IO) {
                try {
                    val prefs = context.getSharedPreferences("salon_settings_prefs", Context.MODE_PRIVATE)
                    val allowBlank = prefs.getBoolean("allow_blank_db", false)
                    if (!allowBlank) {
                        populateInitialData(dbProvider().salonDao())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SalonDatabase", "Error in callback onCreate", e)
                }
            }
        }

        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
            super.onDestructiveMigration(db)
            scope.launch(Dispatchers.IO) {
                try {
                    val prefs = context.getSharedPreferences("salon_settings_prefs", Context.MODE_PRIVATE)
                    val allowBlank = prefs.getBoolean("allow_blank_db", false)
                    if (!allowBlank) {
                        populateInitialData(dbProvider().salonDao())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SalonDatabase", "Error in callback onDestructiveMigration", e)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            scope.launch(Dispatchers.IO) {
                try {
                    val dao = dbProvider().salonDao()
                    val prefs = context.getSharedPreferences("salon_settings_prefs", Context.MODE_PRIVATE)
                    val allowBlank = prefs.getBoolean("allow_blank_db", false)
                    if (!allowBlank && dao.getServicesCount() == 0) {
                        populateInitialData(dao)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SalonDatabase", "Error in callback onOpen", e)
                }
            }
        }
    }
}

suspend fun populateInitialData(dao: SalonDao) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = dateFormat.format(Date())

    // 1. Serviços
    val s1 = dao.insertService(
        SalonService(
            name = "Corte Feminino & Escova",
            category = "Cabelo",
            price = 85.0,
            durationMinutes = 45,
            description = "Lavagem relaxante, corte visagista personalizado e finalização com escova modelada.",
            iconName = "content_cut",
            professionalIdsCsv = "1,3"
        )
    )
    val s2 = dao.insertService(
        SalonService(
            name = "Escova Modelada",
            category = "Cabelo",
            price = 60.0,
            durationMinutes = 40,
            description = "Lavagem com linha profissional e escovação com brilho acetinado e durabilidade.",
            iconName = "air",
            professionalIdsCsv = "1,3"
        )
    )
    val s3 = dao.insertService(
        SalonService(
            name = "Mechas & Loiro Iluminado",
            category = "Química & Cor",
            price = 290.0,
            durationMinutes = 150,
            description = "Técnica de morena iluminada ou loiro dos sonhos com plex protetor e matização.",
            iconName = "auto_awesome",
            professionalIdsCsv = "1"
        )
    )
    val s4 = dao.insertService(
        SalonService(
            name = "Coloração Completa",
            category = "Química & Cor",
            price = 180.0,
            durationMinutes = 90,
            description = "Cobertura de brancos ou mudança de tom com tintura premium e tratamento pós-cor.",
            iconName = "palette",
            professionalIdsCsv = "1"
        )
    )
    val s5 = dao.insertService(
        SalonService(
            name = "Progressiva Orgânica",
            category = "Química & Cor",
            price = 230.0,
            durationMinutes = 120,
            description = "Alisamento seguro sem formol, livre de cheiro forte, fios alinhados e sedosos.",
            iconName = "straighten",
            professionalIdsCsv = "1"
        )
    )
    val s6 = dao.insertService(
        SalonService(
            name = "Hidratação & Cronograma",
            category = "Tratamentos",
            price = 110.0,
            durationMinutes = 50,
            description = "Reposição de água, lipídios e aminoácidos para recuperação instantânea da fibra.",
            iconName = "spa",
            professionalIdsCsv = "1,3"
        )
    )
    val s7 = dao.insertService(
        SalonService(
            name = "Penteado para Festas",
            category = "Cabelo",
            price = 150.0,
            durationMinutes = 60,
            description = "Coques despojados, semipresos, tranças e finalização para formaturas e casamentos.",
            iconName = "face",
            professionalIdsCsv = "3"
        )
    )
    val s8 = dao.insertService(
        SalonService(
            name = "Manicure Tradicional",
            category = "Unhas",
            price = 35.0,
            durationMinutes = 40,
            description = "Cutilagem suave, esfoliação e esmaltação com acabamento impecável.",
            iconName = "brush",
            professionalIdsCsv = "2"
        )
    )
    val s9 = dao.insertService(
        SalonService(
            name = "Pedicure & Spa dos Pés",
            category = "Unhas",
            price = 55.0,
            durationMinutes = 45,
            description = "Imersão relaxante, hidratação profunda, esfoliação e esmaltação.",
            iconName = "clean_hands",
            professionalIdsCsv = "2"
        )
    )

    // 2. Profissionais com serviços que executam
    val p1 = dao.insertProfessional(
        Professional(
            name = "Vanira",
            role = "Cabeleireira & Visagista Master",
            avatarEmoji = "💇‍♀️",
            phone = "(11) 98111-2233",
            rating = 5.0,
            serviceIdsCsv = "$s1,$s2,$s3,$s5,$s6"
        )
    )
    val p2 = dao.insertProfessional(
        Professional(
            name = "Vanessa",
            role = "Colorista & Terapeuta Capilar",
            avatarEmoji = "✨",
            phone = "(11) 98111-4455",
            rating = 5.0,
            serviceIdsCsv = "$s1,$s2,$s3,$s4,$s6,$s7"
        )
    )
    val p3 = dao.insertProfessional(
        Professional(
            name = "Juliana Silva",
            role = "Designer de Unhas & Manicure",
            avatarEmoji = "💅",
            phone = "(11) 98222-3344",
            rating = 4.9,
            serviceIdsCsv = "$s8,$s9"
        )
    )
    val p4 = dao.insertProfessional(
        Professional(
            name = "Beatriz Rocha",
            role = "Penteados & Tratamentos",
            avatarEmoji = "💆‍♀️",
            phone = "(11) 98333-4455",
            rating = 4.8,
            serviceIdsCsv = "$s1,$s2,$s6,$s7"
        )
    )

    // 3. Clientes
    val now = System.currentTimeMillis()
    val dayMillis = 86_400_000L
    val c1 = dao.insertClient(
        Client(
            name = "Ana Carolina Ramos",
            phone = "11987654321",
            birthDate = "14/05/1992",
            address = "Rua Augusta, 450 - SP",
            hairPreferences = "Ondulado 2B, mechas loiro mel com raiz esfumada, evita sulfatos fortes.",
            notes = "Adora café expresso com canela; prefere água morna no lavatório.",
            lastVisitTimestamp = now
        )
    )
    val c2 = dao.insertClient(
        Client(
            name = "Maria Eduarda Lima",
            phone = "11976543210",
            birthDate = "22/09/1988",
            address = "Av. Paulista, 1200 - SP",
            hairPreferences = "Liso 1A natural, pontas retas; adora esmaltes nude clássicos.",
            notes = "Muito pontual, prefere agendamentos na parte da manhã.",
            lastVisitTimestamp = now
        )
    )
    val c3 = dao.insertClient(
        Client(
            name = "Fernanda Souza",
            phone = "11998123456",
            birthDate = "03/11/1995",
            address = "Rua Oscar Freire, 890 - SP",
            hairPreferences = "Cacheado 3A com luzes pontuais, fios finos que exigem bastante nutrição.",
            notes = "Em processo de cronograma capilar; comprou óleo de argan na última visita.",
            lastVisitTimestamp = now - (15 * dayMillis)
        )
    )
    val c4 = dao.insertClient(
        Client(
            name = "Juliana Vasconcelos",
            phone = "11981237788",
            birthDate = "30/01/1986",
            address = "Alameda Lorena, 210 - SP",
            hairPreferences = "Coloração Ruivo Acobreado 7.4 Majirel; couro cabeludo sensível.",
            notes = "Fazer teste de mecha antes de clarear; sempre agenda com a Carla.",
            lastVisitTimestamp = now - (25 * dayMillis)
        )
    )
    val c5 = dao.insertClient(
        Client(
            name = "Camila Fagundes",
            phone = "11972348899",
            birthDate = "19/08/1999",
            address = "Rua Haddock Lobo, 600 - SP",
            hairPreferences = "Loiro pérola; reconstrução mensal com queratina hidrolisada.",
            notes = "Cliente assídua toda quinta-feira.",
            lastVisitTimestamp = now - (68 * dayMillis) // 68 dias atrás (inativa > 60 dias!)
        )
    )
    val c6 = dao.insertClient(
        Client(
            name = "Patrícia Alencar",
            phone = "11993451122",
            birthDate = "11/12/1979",
            address = "Rua Bela Cintra, 730 - SP",
            hairPreferences = "Curto moderno com nuca batida; grisalho natural elegante.",
            notes = "Gosta de corte a seco.",
            lastVisitTimestamp = now - (75 * dayMillis) // 75 dias atrás (inativa > 60 dias!)
        )
    )
    val c7 = dao.insertClient(
        Client(
            name = "Renata Silveira",
            phone = "11984562211",
            birthDate = "05/04/1990",
            address = "Rua Pamplona, 320 - SP",
            hairPreferences = "Cabelo longo volumoso, gosta de ondas largas estilo babyliss.",
            notes = "Sem restrições.",
            lastVisitTimestamp = now
        )
    )
    val c8 = dao.insertClient(
        Client(
            name = "Marcela Dias",
            phone = "11985673322",
            birthDate = "17/07/1994",
            address = "Rua da Consolação, 1500 - SP",
            hairPreferences = "Liso com mechas californianas douradas.",
            notes = "Costuma vir para eventos.",
            lastVisitTimestamp = now
        )
    )

    // 4. Agendamentos de Hoje (8 atendimentos: 5 confirmados, 2 aguardando, 1 cancelado)
    dao.insertAppointments(
        listOf(
            Appointment(
                clientName = "Ana Carolina Ramos",
                clientPhone = "11987654321",
                clientId = c1,
                serviceId = s1,
                serviceName = "Corte Feminino & Escova",
                professionalId = p1,
                professionalName = "Carla Mendes",
                dateStr = todayStr,
                timeStr = "09:00",
                durationMinutes = 45,
                price = 85.0,
                status = "CONFIRMADO"
            ),
            Appointment(
                clientName = "Maria Eduarda Lima",
                clientPhone = "11976543210",
                clientId = c2,
                serviceId = s9,
                serviceName = "Pedicure & Spa dos Pés",
                professionalId = p2,
                professionalName = "Juliana Silva",
                dateStr = todayStr,
                timeStr = "10:30",
                durationMinutes = 45,
                price = 55.0,
                status = "CONFIRMADO"
            ),
            Appointment(
                clientName = "Fernanda Souza",
                clientPhone = "11998123456",
                clientId = c3,
                serviceId = s3,
                serviceName = "Mechas & Loiro Iluminado",
                professionalId = p1,
                professionalName = "Carla Mendes",
                dateStr = todayStr,
                timeStr = "13:00",
                durationMinutes = 150,
                price = 290.0,
                status = "CONFIRMADO"
            ),
            Appointment(
                clientName = "Camila Fagundes",
                clientPhone = "11972348899",
                clientId = c5,
                serviceId = s8,
                serviceName = "Manicure Tradicional",
                professionalId = p2,
                professionalName = "Juliana Silva",
                dateStr = todayStr,
                timeStr = "14:00",
                durationMinutes = 40,
                price = 35.0,
                status = "AGUARDANDO"
            ),
            Appointment(
                clientName = "Juliana Vasconcelos",
                clientPhone = "11981237788",
                clientId = c4,
                serviceId = s4,
                serviceName = "Coloração Completa",
                professionalId = p1,
                professionalName = "Carla Mendes",
                dateStr = todayStr,
                timeStr = "15:30",
                durationMinutes = 90,
                price = 180.0,
                status = "CONFIRMADO"
            ),
            Appointment(
                clientName = "Patrícia Alencar",
                clientPhone = "11993451122",
                clientId = c6,
                serviceId = s1,
                serviceName = "Corte Feminino & Escova",
                professionalId = p3,
                professionalName = "Beatriz Rocha",
                dateStr = todayStr,
                timeStr = "16:30",
                durationMinutes = 45,
                price = 85.0,
                status = "AGUARDANDO"
            ),
            Appointment(
                clientName = "Renata Silveira",
                clientPhone = "11984562211",
                clientId = c7,
                serviceId = s6,
                serviceName = "Hidratação & Cronograma",
                professionalId = p3,
                professionalName = "Beatriz Rocha",
                dateStr = todayStr,
                timeStr = "17:30",
                durationMinutes = 50,
                price = 110.0,
                status = "CONFIRMADO"
            ),
            Appointment(
                clientName = "Marcela Dias",
                clientPhone = "11985673322",
                clientId = c8,
                serviceId = s7,
                serviceName = "Penteado para Festas",
                professionalId = p3,
                professionalName = "Beatriz Rocha",
                dateStr = todayStr,
                timeStr = "18:30",
                durationMinutes = 60,
                price = 150.0,
                status = "CANCELADO"
            )
        )
    )

    // 5. Transações Financeiras (Recebidas e Contas a Receber)
    dao.insertTransactions(
        listOf(
            PaymentTransaction(
                clientName = "Ana Carolina Ramos",
                serviceName = "Corte Feminino & Escova",
                amount = 85.0,
                dateStr = todayStr,
                paymentMethod = "PIX",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Maria Eduarda Lima",
                serviceName = "Pedicure & Spa dos Pés",
                amount = 55.0,
                dateStr = todayStr,
                paymentMethod = "CARTAO_DEBITO",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Juliana Vasconcelos",
                serviceName = "Coloração Completa",
                amount = 180.0,
                dateStr = todayStr,
                paymentMethod = "CARTAO_CREDITO",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Fernanda Souza",
                serviceName = "Mechas & Loiro Iluminado",
                amount = 290.0,
                dateStr = todayStr,
                paymentMethod = "PENDENTE",
                status = "PENDENTE",
                dueDate = todayStr,
                notes = "Aguardando confirmação de transferência PIX"
            ),
            PaymentTransaction(
                clientName = "Renata Silveira",
                serviceName = "Hidratação & Cronograma",
                amount = 110.0,
                dateStr = todayStr,
                paymentMethod = "PENDENTE",
                status = "PENDENTE",
                dueDate = todayStr,
                notes = "Pagamento na saída do salão"
            ),
            // Histórico recente para faturamento do mês
            PaymentTransaction(
                clientName = "Carla Medeiros",
                serviceName = "Progressiva Orgânica",
                amount = 230.0,
                dateStr = "2026-09-20",
                paymentMethod = "CARTAO_CREDITO",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Talita Nogueira",
                serviceName = "Mechas & Loiro Iluminado",
                amount = 290.0,
                dateStr = "2026-09-22",
                paymentMethod = "PIX",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Bianca Castro",
                serviceName = "Manicure Tradicional",
                amount = 35.0,
                dateStr = "2026-09-23",
                paymentMethod = "DINHEIRO",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Luciana Prado",
                serviceName = "Corte Feminino & Escova",
                amount = 85.0,
                dateStr = "2026-09-25",
                paymentMethod = "PIX",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Cláudia Duarte",
                serviceName = "Penteado para Festas",
                amount = 150.0,
                dateStr = "2026-09-25",
                paymentMethod = "CARTAO_CREDITO",
                status = "PAGO"
            ),
            PaymentTransaction(
                clientName = "Vanessa Queiroz",
                serviceName = "Tratamento Capilar & Mechas",
                amount = 320.0,
                dateStr = "2026-09-18",
                paymentMethod = "PENDENTE",
                status = "PENDENTE",
                dueDate = "2026-09-25",
                notes = "Cobrança enviada por WhatsApp"
            )
        )
    )

    // 5. Produtos e Controle de Estoque
    dao.insertProducts(
        listOf(
            Product(
                name = "Óleo Oil Reflections 100ml",
                brand = "Wella Professionals",
                category = "Finalizadores",
                quantityInStock = 8,
                minStockAlert = 3,
                costPrice = 85.0,
                sellPrice = 145.0,
                barcode = "78912345601",
                description = "Nutrição e brilho luminoso com óleo de semente de macadâmia e abacate."
            ),
            Product(
                name = "Máscara Nutri Enrich 500g",
                brand = "Wella Professionals",
                category = "Tratamento",
                quantityInStock = 4,
                minStockAlert = 2,
                costPrice = 120.0,
                sellPrice = 210.0,
                barcode = "78912345602",
                description = "Nutrição instantânea para fios secos e desgastados com Goji Berry e Ácido Oleico."
            ),
            Product(
                name = "Truss Uso Obrigatório 260ml",
                brand = "Truss Professional",
                category = "Finalizadores",
                quantityInStock = 12,
                minStockAlert = 4,
                costPrice = 75.0,
                sellPrice = 135.0,
                barcode = "78912345603",
                description = "Reconstrutor capilar com proteção térmica, sela cutículas e anti-frizz."
            ),
            Product(
                name = "Kit Cronograma Capilar Braé Revival",
                brand = "Braé Hair Care",
                category = "Home Care",
                quantityInStock = 5,
                minStockAlert = 3,
                costPrice = 140.0,
                sellPrice = 250.0,
                barcode = "78912345604",
                description = "Kit completo com Shampoo, Condicionador e Ampola de resgate imediato."
            ),
            Product(
                name = "Coloração Color Touch 60g",
                brand = "Wella Professionals",
                category = "Coloração",
                quantityInStock = 15,
                minStockAlert = 5,
                costPrice = 32.0,
                sellPrice = 58.0,
                barcode = "78912345605",
                description = "Tonalizante multidimensional sem amônia para nuances personalizadas."
            ),
            Product(
                name = "Elixir Ultime L'Huile Originale 100ml",
                brand = "Kérastase",
                category = "Finalizadores",
                quantityInStock = 2, // Alerta de estoque baixo!
                minStockAlert = 3,
                costPrice = 180.0,
                sellPrice = 310.0,
                barcode = "78912345606",
                description = "Óleo sublime com extrato de camélia francesa para brilho incomparável."
            )
        )
    )

    // 6. Bloqueios de Horários Iniciais
    dao.insertBlock(
        ScheduleBlock(
            professionalId = p1,
            dateStr = todayStr,
            startTime = "12:00",
            endTime = "13:00",
            reason = "Intervalo de Almoço - Vanira"
        )
    )
    dao.insertBlock(
        ScheduleBlock(
            professionalId = p2,
            dateStr = todayStr,
            startTime = "12:30",
            endTime = "13:30",
            reason = "Intervalo de Almoço - Vanessa"
        )
    )
}
