package com.example.medreminder.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.medreminder.data.entity.AdherenceRecord
import kotlinx.coroutines.flow.Flow

/** 按药品分组的依从性聚合结果 */
data class DrugAdherence(
    val drugName: String,
    val total: Int,
    val taken: Int,
    val late: Int,
    val skipped: Int,
    val missed: Int
)

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

    @Query("SELECT * FROM adherence_record WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AdherenceRecord?

    /** 撤销打卡：仅允许在 taken_at 之后 5 分钟内，将已打卡记录还原为待处理 */
    @Query("UPDATE adherence_record SET status = :pending, actual_time = NULL, taken_at = NULL WHERE id = :id AND status IN (:taken, :late)")
    suspend fun undoTake(id: Long, pending: String, taken: String, late: String): Int

    /** 按药品分组的依从性统计 */
    @Query(
        """
        SELECT drugName,
               COUNT(*) AS total,
               SUM(CASE WHEN status = 'TAKEN' THEN 1 ELSE 0 END) AS taken,
               SUM(CASE WHEN status = 'LATE' THEN 1 ELSE 0 END) AS late,
               SUM(CASE WHEN status = 'SKIPPED' THEN 1 ELSE 0 END) AS skipped,
               SUM(CASE WHEN status = 'MISSED' THEN 1 ELSE 0 END) AS missed
        FROM adherence_record
        GROUP BY drugName
        ORDER BY drugName
        """
    )
    fun observeAdherenceByDrug(): Flow<List<DrugAdherence>>

    @Query("DELETE FROM adherence_record")
    suspend fun clear()
}
