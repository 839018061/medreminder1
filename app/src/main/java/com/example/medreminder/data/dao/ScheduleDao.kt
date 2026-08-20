package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.medreminder.data.entity.DoseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM dose_schedule WHERE active = 1 ORDER BY time")
    fun observeActive(): Flow<List<DoseSchedule>>

    @Query("SELECT * FROM dose_schedule ORDER BY time")
    suspend fun getAll(): List<DoseSchedule>

    @Insert
    suspend fun insert(schedule: DoseSchedule): Long

    @Update
    suspend fun update(schedule: DoseSchedule)

    @Query("UPDATE dose_schedule SET active = 0 WHERE id = :id")
    suspend fun deactivate(id: Long)

    @Query("DELETE FROM dose_schedule WHERE drugId = :drugId")
    suspend fun deleteByDrug(drugId: Long)

    @Query("DELETE FROM dose_schedule")
    suspend fun clear()
}
