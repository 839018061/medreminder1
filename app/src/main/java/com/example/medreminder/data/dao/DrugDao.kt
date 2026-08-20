package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.medreminder.data.entity.Drug
import kotlinx.coroutines.flow.Flow

@Dao
interface DrugDao {

    @Query("SELECT * FROM drug WHERE active = 1 ORDER BY id")
    fun observeActive(): Flow<List<Drug>>

    @Query("SELECT * FROM drug ORDER BY id")
    suspend fun getAll(): List<Drug>

    @Insert
    suspend fun insert(drug: Drug): Long

    @Update
    suspend fun update(drug: Drug)

    @Query("UPDATE drug SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM drug")
    suspend fun clear()
}
