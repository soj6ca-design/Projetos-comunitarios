package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Appointment
import com.example.data.model.Client
import com.example.data.model.PaymentTransaction
import com.example.data.model.Product
import com.example.data.model.Professional
import com.example.data.model.SalonService
import com.example.data.model.ScheduleBlock
import kotlinx.coroutines.flow.Flow

@Dao
interface SalonDao {

    // --- Professionals ---
    @Query("SELECT * FROM professionals ORDER BY name ASC")
    fun getAllProfessionals(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE active = 1 ORDER BY name ASC")
    fun getActiveProfessionals(): Flow<List<Professional>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: Professional): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionals(professionals: List<Professional>)

    @Update
    suspend fun updateProfessional(professional: Professional)

    @Delete
    suspend fun deleteProfessional(professional: Professional)

    // --- Clients ---
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun getAllClients(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchClients(query: String): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): Client?

    @Query("SELECT * FROM clients WHERE lastVisitTimestamp <= :cutoffTimestamp ORDER BY lastVisitTimestamp ASC")
    fun getInactiveClients(cutoffTimestamp: Long): Flow<List<Client>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: Client): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClients(clients: List<Client>)

    @Update
    suspend fun updateClient(client: Client)

    @Delete
    suspend fun deleteClient(client: Client)

    // --- Services ---
    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServicesCount(): Int

    @Query("SELECT * FROM services ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<SalonService>>

    @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
    suspend fun getServiceById(id: Long): SalonService?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: SalonService): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<SalonService>)

    @Update
    suspend fun updateService(service: SalonService)

    @Delete
    suspend fun deleteService(service: SalonService)

    // --- Appointments ---
    @Query("SELECT * FROM appointments ORDER BY dateStr DESC, timeStr ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateStr = :dateStr ORDER BY timeStr ASC")
    fun getAppointmentsByDate(dateStr: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE clientName = :clientName OR clientId = :clientId ORDER BY dateStr DESC")
    fun getAppointmentsForClient(clientName: String, clientId: Long?): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE professionalId = :profId AND dateStr = :dateStr ORDER BY timeStr ASC")
    fun getAppointmentsByProfessionalAndDate(profId: Long, dateStr: String): Flow<List<Appointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)

    // --- Transactions / Financeiro ---
    @Query("SELECT * FROM transactions ORDER BY dateStr DESC, id DESC")
    fun getAllTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM transactions WHERE status = 'PENDENTE' ORDER BY dueDate ASC, id DESC")
    fun getPendingTransactions(): Flow<List<PaymentTransaction>>

    @Query("SELECT * FROM transactions WHERE dateStr = :dateStr ORDER BY id DESC")
    fun getTransactionsByDate(dateStr: String): Flow<List<PaymentTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PaymentTransaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<PaymentTransaction>)

    @Update
    suspend fun updateTransaction(transaction: PaymentTransaction)

    @Query("UPDATE transactions SET status = 'PAGO', paymentMethod = :method, paidAt = :paidAt WHERE id = :id")
    suspend fun markTransactionPaid(id: Long, method: String, paidAt: Long)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    // --- Schedule Blocks ---
    @Query("SELECT * FROM schedule_blocks ORDER BY dateStr DESC")
    fun getAllScheduleBlocks(): Flow<List<ScheduleBlock>>

    @Query("SELECT * FROM schedule_blocks WHERE dateStr = :dateStr")
    fun getBlocksByDate(dateStr: String): Flow<List<ScheduleBlock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: ScheduleBlock): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<ScheduleBlock>)

    @Delete
    suspend fun deleteBlock(block: ScheduleBlock)

    @Query("DELETE FROM schedule_blocks WHERE id = :id")
    suspend fun deleteBlockById(id: Long)

    // --- Products / Estoque ---
    @Query("SELECT * FROM products ORDER BY category ASC, name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET quantityInStock = :newQuantity WHERE id = :id")
    suspend fun updateProductStock(id: Long, newQuantity: Int)

    // --- Client Portal Lookup ---
    @Query("SELECT * FROM clients WHERE token = :token OR phone = :token LIMIT 1")
    suspend fun getClientByTokenOrPhone(token: String): Client?

    @Query("SELECT * FROM appointments WHERE clientPhone = :phone ORDER BY dateStr DESC, timeStr DESC")
    fun getAppointmentsByClientPhone(phone: String): Flow<List<Appointment>>

    // --- Limpeza e Reset de Dados ---
    @Query("DELETE FROM appointments")
    suspend fun clearAllAppointments()

    @Query("DELETE FROM transactions")
    suspend fun clearAllTransactions()

    @Query("DELETE FROM schedule_blocks")
    suspend fun clearAllScheduleBlocks()

    @Query("DELETE FROM clients")
    suspend fun clearAllClients()

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()

    @Query("DELETE FROM services")
    suspend fun clearAllServices()

    @Query("DELETE FROM professionals")
    suspend fun clearAllProfessionals()

    // --- Consultas Diretas para Backup ---
    @Query("SELECT * FROM professionals")
    suspend fun getProfessionalsList(): List<Professional>

    @Query("SELECT * FROM clients")
    suspend fun getClientsList(): List<Client>

    @Query("SELECT * FROM services")
    suspend fun getServicesList(): List<SalonService>

    @Query("SELECT * FROM appointments")
    suspend fun getAppointmentsList(): List<Appointment>

    @Query("SELECT * FROM transactions")
    suspend fun getTransactionsList(): List<PaymentTransaction>

    @Query("SELECT * FROM schedule_blocks")
    suspend fun getScheduleBlocksList(): List<ScheduleBlock>

    @Query("SELECT * FROM products")
    suspend fun getProductsList(): List<Product>
}
