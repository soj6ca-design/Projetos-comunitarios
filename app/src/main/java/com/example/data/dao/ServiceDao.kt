package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SalonService
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {

    @Query("SELECT COUNT(*) FROM services")
    suspend fun getServicesCount(): Int

    @Query("SELECT * FROM services ORDER BY category ASC, name ASC")
    fun getAllServices(): Flow<List<SalonService>>

    @Query("SELECT * FROM services WHERE category = :category ORDER BY name ASC")
    fun getServicesByCategory(category: String): Flow<List<SalonService>>

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

    @Query("DELETE FROM services WHERE id = :id")
    suspend fun deleteServiceById(id: Long)
}
