package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medreminder.data.entity.AdherenceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: AdherenceRecord): Long

    @Query("SELECT * FROM adherence_records WHERE drugId = :drugId")
    fun getForDrug(drugId: Long): Flow<List<AdherenceRecord>>

    @Query("SELECT * FROM adherence_records")
    fun getAll(): Flow<List<AdherenceRecord>>

    @Query("SELECT * FROM adherence_records WHERE date = :date")
    suspend fun getByDate(date: String): List<AdherenceRecord>
}
