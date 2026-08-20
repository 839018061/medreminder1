package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medreminder.data.entity.DoseSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM dose_schedules WHERE drugId = :drugId")
    fun getForDrug(drugId: Long): Flow<List<DoseSchedule>>

    @Query("SELECT * FROM dose_schedules")
    fun getAll(): Flow<List<DoseSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: DoseSchedule): Long

    @Update
    suspend fun update(schedule: DoseSchedule)

    @Delete
    suspend fun delete(schedule: DoseSchedule)

    @Query("DELETE FROM dose_schedules WHERE drugId = :drugId")
    suspend fun deleteByDrugId(drugId: Long)
}
