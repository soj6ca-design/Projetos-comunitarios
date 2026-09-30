package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Appointment
import com.example.data.model.AppointmentWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {

    @Query("SELECT * FROM appointments ORDER BY dateStr DESC, timeStr ASC")
    fun getAllAppointments(): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE dateStr = :dateStr ORDER BY timeStr ASC")
    fun getAppointmentsByDate(dateStr: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE clientName = :clientName OR clientId = :clientId ORDER BY dateStr DESC")
    fun getAppointmentsForClient(clientName: String, clientId: Long?): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE professionalId = :profId AND dateStr = :dateStr ORDER BY timeStr ASC")
    fun getAppointmentsByProfessionalAndDate(profId: Long, dateStr: String): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentById(id: Long): Appointment?

    @Transaction
    @Query("SELECT * FROM appointments WHERE id = :id LIMIT 1")
    suspend fun getAppointmentWithDetails(id: Long): AppointmentWithDetails?

    @Transaction
    @Query("SELECT * FROM appointments WHERE dateStr = :dateStr ORDER BY timeStr ASC")
    fun getAppointmentsWithDetailsByDate(dateStr: String): Flow<List<AppointmentWithDetails>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: Appointment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointments(appointments: List<Appointment>)

    @Update
    suspend fun updateAppointment(appointment: Appointment)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateAppointmentStatus(id: Long, status: String)

    @Delete
    suspend fun deleteAppointment(appointment: Appointment)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointmentById(id: Long)
}
