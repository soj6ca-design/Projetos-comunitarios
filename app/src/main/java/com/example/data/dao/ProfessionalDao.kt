package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Professional
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfessionalDao {

    @Query("SELECT * FROM professionals ORDER BY name ASC")
    fun getAllProfessionals(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE active = 1 ORDER BY name ASC")
    fun getActiveProfessionals(): Flow<List<Professional>>

    @Query("SELECT * FROM professionals WHERE id = :id LIMIT 1")
    suspend fun getProfessionalById(id: Long): Professional?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessional(professional: Professional): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfessionals(professionals: List<Professional>)

    @Update
    suspend fun updateProfessional(professional: Professional)

    @Delete
    suspend fun deleteProfessional(professional: Professional)

    @Query("DELETE FROM professionals WHERE id = :id")
    suspend fun deleteProfessionalById(id: Long)
}
