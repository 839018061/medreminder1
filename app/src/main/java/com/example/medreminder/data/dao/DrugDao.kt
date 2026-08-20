package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medreminder.data.entity.Drug
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugDao {
    @Query("SELECT * FROM drugs ORDER BY id DESC")
    fun getAll(): Flow<List<Drug>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(drug: Drug): Long

    @Update
    suspend fun update(drug: Drug)

    @Delete
    suspend fun delete(drug: Drug)

    @Query("DELETE FROM drugs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM drugs WHERE id = :id")
    suspend fun getById(id: Long): Drug?
}
