package com.example.medreminder.data.repository

import com.example.medreminder.data.AppDatabase
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val db: AppDatabase) {
    fun observeDrugs(): Flow<List<Drug>> = db.drugDao().getAll()
    fun observeSchedules(drugId: Long): Flow<List<DoseSchedule>> = db.scheduleDao().getForDrug(drugId)
    fun observeAllSchedules(): Flow<List<DoseSchedule>> = db.scheduleDao().getAll()
    fun observeRecords(drugId: Long): Flow<List<AdherenceRecord>> = db.recordDao().getForDrug(drugId)

    suspend fun saveDrug(drug: Drug): Long = db.drugDao().insert(drug)
    suspend fun updateDrug(drug: Drug) = db.drugDao().update(drug)
    suspend fun deleteDrug(drug: Drug) = db.drugDao().delete(drug)
    suspend fun deleteDrugById(id: Long) = db.drugDao().deleteById(id)

    suspend fun saveSchedule(schedule: DoseSchedule): Long = db.scheduleDao().insert(schedule)
    suspend fun updateSchedule(schedule: DoseSchedule) = db.scheduleDao().update(schedule)
    suspend fun deleteSchedule(schedule: DoseSchedule) = db.scheduleDao().delete(schedule)
    suspend fun deleteSchedulesForDrug(drugId: Long) = db.scheduleDao().deleteByDrugId(drugId)

    suspend fun saveRecord(record: AdherenceRecord): Long = db.recordDao().insert(record)
    suspend fun getRecordsByDate(date: String): List<AdherenceRecord> = db.recordDao().getByDate(date)
    suspend fun getAllRecords(): Flow<List<AdherenceRecord>> = db.recordDao().getAll()
}
