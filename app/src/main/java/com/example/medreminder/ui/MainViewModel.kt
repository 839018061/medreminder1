package com.example.medreminder.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.medreminder.data.AppDatabase
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.data.repository.MedicationRepository
import com.example.medreminder.reminder.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = MedicationRepository(AppDatabase.getInstance(app))

    val drugs: StateFlow<List<Drug>> = repository.observeDrugs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDrug(drug: Drug, schedules: List<DoseSchedule>) {
        viewModelScope.launch {
            val id = repository.saveDrug(drug)
            repository.deleteSchedulesForDrug(id)
            schedules.forEach { s ->
                val saved = repository.saveSchedule(s.copy(drugId = id))
                AlarmScheduler.schedule(getApplication(), s.copy(id = saved, drugId = id))
            }
        }
    }

    fun deleteDrug(drug: Drug) {
        viewModelScope.launch {
            repository.observeAllSchedules().collect { all ->
                all.filter { it.drugId == drug.id }.forEach { AlarmScheduler.cancel(getApplication(), it) }
            }
            repository.deleteSchedulesForDrug(drug.id)
            repository.deleteDrug(drug)
        }
    }
}
