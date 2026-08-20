package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.medreminder.data.entity.AdherenceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query("SELECT * FROM adherence_record ORDER BY planTime DESC")
    fun observeAll(): Flow<List<AdherenceRecord>>

    @Query("SELECT * FROM adherence_record WHERE planDate = :date ORDER BY planTime")
    fun observeByDate(date: String): Flow<List<AdherenceRecord>>

    @Query("SELECT * FROM adherence_record WHERE scheduleId = :scheduleId AND planDate = :date LIMIT 1")
    suspend fun find(scheduleId: Long, date: String): AdherenceRecord?

    @Insert
    suspend fun insert(record: AdherenceRecord): Long

    @Update
    suspend fun update(record: AdherenceRecord)

    @Query("DELETE FROM adherence_record")
    suspend fun clear()
}
