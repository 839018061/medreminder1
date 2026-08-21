package com.example.medreminder.ui

import android.app.Application
import android.content.Context
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
import com.example.medreminder.util.PermissionChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as MedReminderApp).repository

    val drugs = repo.drugs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val schedules = repo.schedules.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val records = repo.records.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val owners = repo.owners.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val adherenceByDrug = repo.adherenceByDrug.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 完成打卡庆祝：最近一次完成打卡的药品名，null 表示无 */
    private val _celebration = MutableStateFlow<String?>(null)
    val celebration = _celebration

    /** 撤销提示：撤销成功后触发一次性事件（药品名） */
    private val _undoMessage = MutableStateFlow<String?>(null)
    val undoMessage = _undoMessage

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

    /** 已服药（按时） */
    fun markTaken(schedule: DoseSchedule, drug: Drug) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repo.recordFor(schedule.id, DateUtils.today())
            repo.insertOrUpdateRecord(
                (existing ?: newRecord(schedule, drug)).copy(
                    status = AdherenceRecord.TAKEN,
                    actualTime = now,
                    takenAt = now
                )
            )
            _celebration.value = drug.name
        }
    }

    /** 补服（迟服） */
    fun markLate(schedule: DoseSchedule, drug: Drug) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = repo.recordFor(schedule.id, DateUtils.today())
            repo.insertOrUpdateRecord(
                (existing ?: newRecord(schedule, drug)).copy(
                    status = AdherenceRecord.LATE,
                    actualTime = now,
                    takenAt = now
                )
            )
            _celebration.value = drug.name
        }
    }

    /** 跳过本次 */
    fun markSkip(schedule: DoseSchedule, drug: Drug) {
        viewModelScope.launch {
            val existing = repo.recordFor(schedule.id, DateUtils.today())
            repo.insertOrUpdateRecord(
                (existing ?: newRecord(schedule, drug)).copy(
                    status = AdherenceRecord.SKIPPED,
                    actualTime = null,
                    takenAt = null
                )
            )
        }
    }

    /** 撤销打卡（5 分钟窗口内） */
    fun undoTake(recordId: Long) {
        viewModelScope.launch {
            val ok = repo.undoTake(recordId)
            if (ok) _undoMessage.value = "已撤销打卡"
        }
    }

    fun consumeCelebration() {
        _celebration.value = null
    }

    fun consumeUndoMessage() {
        _undoMessage.value = null
    }

    /** 把已过期仍未处理的 PENDING/SNOOZED 记录统一补标记为 MISSED（次日进入首页时调用） */
    fun finalizeOverdueRecords() {
        viewModelScope.launch {
            val today = DateUtils.today()
            records.value
                .filter {
                    it.planDate < today &&
                        (it.status == AdherenceRecord.PENDING || it.status == AdherenceRecord.SNOOZED)
                }
                .forEach { repo.insertOrUpdateRecord(it.copy(status = AdherenceRecord.MISSED)) }
        }
    }

    private fun newRecord(schedule: DoseSchedule, drug: Drug) = AdherenceRecord(
        scheduleId = schedule.id,
        drugName = drug.name,
        planDate = DateUtils.today(),
        planTime = DateUtils.todayAtMillis(schedule.time),
        actualTime = null,
        status = AdherenceRecord.PENDING
    )

    fun importRegimen(regimen: Regimen) {
        viewModelScope.launch {
            RegimenCodec.regimenToEntities(regimen).forEach { (drug, times) ->
                repo.addDrug(drug, times)
            }
            rescheduleAll(getApplication())
        }
    }

    // ---- 权限引导 ----

    private fun prefs() = getApplication<Application>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    /** 首次启动且精确闹钟/通知权限有缺失时，应自动进入权限引导 */
    fun shouldAutoShowPermissionGuide(): Boolean {
        val app = getApplication<Application>()
        if (prefs().getBoolean("permission_guide_auto_shown", false)) return false
        val hasIssue = !PermissionChecker.isExactAlarmEnabled(app) ||
            !PermissionChecker.isNotificationEnabled(app)
        return hasIssue
    }

    fun markPermissionGuideAutoShown() {
        prefs().edit().putBoolean("permission_guide_auto_shown", true).apply()
    }

    /** 设置图标角标：当前是否有权限未开启 */
    val hasPermissionIssue = MutableStateFlow(false)

    fun refreshPermissionIssue() {
        val app = getApplication<Application>()
        val hasIssue = !PermissionChecker.isExactAlarmEnabled(app) ||
            !PermissionChecker.isNotificationEnabled(app) ||
            !PermissionChecker.isIgnoringBatteryOptimizations(app)
        hasPermissionIssue.value = hasIssue
    }
}
