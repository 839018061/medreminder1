package com.example.medreminder.data.repository

import com.example.medreminder.data.dao.DrugDao
import com.example.medreminder.data.dao.RecordDao
import com.example.medreminder.data.dao.ScheduleDao
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import kotlinx.coroutines.flow.Flow

class MedicationRepository(
    private val drugDao: DrugDao,
    private val scheduleDao: ScheduleDao,
    private val recordDao: RecordDao
) {
    val drugs: Flow<List<Drug>> = drugDao.observeActive()
    val schedules: Flow<List<DoseSchedule>> = scheduleDao.observeActive()
    val records: Flow<List<AdherenceRecord>> = recordDao.observeAll()

    fun recordsByDate(date: String): Flow<List<AdherenceRecord>> = recordDao.observeByDate(date)

    suspend fun allDrugs(): List<Drug> = drugDao.getAll()
    suspend fun allSchedules(): List<DoseSchedule> = scheduleDao.getAll()

    suspend fun addDrug(drug: Drug, schedules: List<DoseSchedule>) {
        val id = drugDao.insert(drug)
        schedules.forEach { scheduleDao.insert(it.copy(drugId = id)) }
    }

    suspend fun updateDrug(drug: Drug, schedules: List<DoseSchedule>) {
        drugDao.update(drug)
        scheduleDao.deleteByDrug(drug.id)
        schedules.forEach { scheduleDao.insert(it.copy(drugId = drug.id)) }
    }

    suspend fun deactivateDrug(drugId: Long) {
        drugDao.deactivate(drugId)
        scheduleDao.deleteByDrug(drugId)
    }

    suspend fun scheduleById(id: Long): DoseSchedule? = scheduleDao.getAll().find { it.id == id }
    suspend fun drugById(id: Long): Drug? = drugDao.getAll().find { it.id == id }

    suspend fun recordFor(scheduleId: Long, date: String): AdherenceRecord? =
        recordDao.find(scheduleId, date)

    suspend fun insertOrUpdateRecord(record: AdherenceRecord) {
        val existing = recordDao.find(record.scheduleId, record.planDate)
        if (existing == null) recordDao.insert(record)
        else recordDao.update(record.copy(id = existing.id))
    }

    suspend fun clearAll() {
        recordDao.clear()
        scheduleDao.clear()
        drugDao.clear()
    }
}
