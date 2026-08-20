package com.example.medreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medreminder.MedReminderApp
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.importexport.Regimen
import com.example.medreminder.importexport.RegimenCodec
import com.example.medreminder.reminder.AlarmScheduler
import com.example.medreminder.reminder.rescheduleAll
import com.example.medreminder.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as MedReminderApp).repository

    val drugs = repo.drugs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val schedules = repo.schedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val records = repo.records.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addDrug(drug: Drug, times: List<DoseSchedule>) {
        viewModelScope.launch {
            repo.addDrug(drug, times)
            rescheduleAll(getApplication())
        }
    }

    fun deleteDrug(drug: Drug) {
        viewModelScope.launch {
            val schs = repo.allSchedules().filter { it.drugId == drug.id }
            schs.forEach { AlarmScheduler.cancel(getApplication(), it.id) }
            repo.deactivateDrug(drug.id)
        }
    }

    fun markTaken(schedule: DoseSchedule, drug: Drug) {
        viewModelScope.launch {
            repo.insertOrUpdateRecord(
                AdherenceRecord(
                    scheduleId = schedule.id,
                    drugName = drug.name,
                    planDate = DateUtils.today(),
                    planTime = DateUtils.todayAtMillis(schedule.time),
                    actualTime = System.currentTimeMillis(),
                    status = AdherenceRecord.TAKEN
                )
            )
        }
    }

    fun markLate(schedule: DoseSchedule, drug: Drug) {
        viewModelScope.launch {
            repo.insertOrUpdateRecord(
                AdherenceRecord(
                    scheduleId = schedule.id,
                    drugName = drug.name,
                    planDate = DateUtils.today(),
                    planTime = DateUtils.todayAtMillis(schedule.time),
                    actualTime = System.currentTimeMillis(),
                    status = AdherenceRecord.LATE
                )
            )
        }
    }

    fun importRegimen(regimen: Regimen) {
        viewModelScope.launch {
            RegimenCodec.regimenToEntities(regimen).forEach { (drug, times) ->
                repo.addDrug(drug, times)
            }
            rescheduleAll(getApplication())
        }
    }
}
