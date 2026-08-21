package com.example.medreminder.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medreminder.data.entity.AdherenceRecord
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.theme.BrandGreen
import com.example.medreminder.ui.theme.ErrorRed
import com.example.medreminder.ui.theme.WarmOrange
import com.example.medreminder.util.DateUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    onAdd: () -> Unit,
    onStats: () -> Unit,
    onImport: () -> Unit,
    onPermissions: () -> Unit
) {
    val drugs by vm.drugs.collectAsState()
    val schedules by vm.schedules.collectAsState()
    val records by vm.records.collectAsState()
    val celebration by vm.celebration.collectAsState()
    val undoMessage by vm.undoMessage.collectAsState()
    val hasPermissionIssue by vm.hasPermissionIssue.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    val today = DateUtils.today()
    val todaySchedules = schedules
        .filter { DateUtils.isScheduledToday(it.repeatDays) }
        .sortedBy { it.time }
    val todayRecordMap = records.filter { it.planDate == today }.associateBy { it.scheduleId }

    // 进入首页时，把过期未处理记录补标记为漏服，并刷新权限状态
    LaunchedEffect(Unit) {
        vm.finalizeOverdueRecords()
        vm.refreshPermissionIssue()
    }

    // 完成庆祝：3 秒后自动关闭
    LaunchedEffect(celebration) {
        if (celebration != null) {
            delay(3000)
            vm.consumeCelebration()
        }
    }
    LaunchedEffect(undoMessage) {
        undoMessage?.let { snackbar.showSnackbar(it); vm.consumeUndoMessage() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用药提醒", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = onPermissions) {
                            Icon(Icons.Default.Settings, "权限与后台设置", tint = Color.White)
                        }
                        if (hasPermissionIssue) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(10.dp)
                                    .background(ErrorRed, CircleShape)
                            )
                        }
                    }
                    IconButton(onClick = onImport) {
                        Icon(Icons.Default.QrCodeScanner, "导入方案", tint = Color.White)
                    }
                    IconButton(onClick = onStats) {
                        Icon(Icons.Default.BarChart, "统计", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加药品", tint = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { SectionTitle("今日服药") }

                if (todaySchedules.isEmpty()) {
                    item { EmptyHint("今天没有服药安排，点击右下角添加药品") }
                } else {
                    items(todaySchedules) { s ->
                        val drug = drugs.find { it.id == s.drugId }
                        DoseCard(s, drug, todayRecordMap[s.id], vm)
                    }
                }

                item { SectionTitle("用药方案") }
                if (drugs.isEmpty()) {
                    item { EmptyHint("还没有用药方案") }
                } else {
                    // 按用药人分组展示
                    val grouped = drugs.groupBy { it.owner }
                    grouped.forEach { (ownerName, ownerDrugs) ->
                        item {
                            Text(
                                ownerName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        items(ownerDrugs) { drug ->
                            DrugRow(
                                drug = drug,
                                times = schedules.filter { it.drugId == drug.id }.map { it.time },
                                onDelete = { vm.deleteDrug(drug) }
                            )
                        }
                    }
                }
            }

            // 完成庆祝动画
            celebration?.let { name ->
                CelebrationOverlay(name)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun EmptyHint(text: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            text,
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/** 完成打卡庆祝浮层 */
@Composable
private fun CelebrationOverlay(drugName: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = BrandGreen,
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "太棒了！已按时服药",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "$drugName",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun DoseCard(
    schedule: DoseSchedule,
    drug: Drug?,
    record: AdherenceRecord?,
    vm: MainViewModel
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    schedule.time,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "${drug?.name ?: "未知药品"}" +
                            if (drug != null && drug.owner != "我") "（${drug.owner}）" else "",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "${drug?.dosage ?: ""}" +
                            if (schedule.relation.isNotBlank() && schedule.relation != "无")
                                " · ${schedule.relation}" else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            when (record?.status) {
                AdherenceRecord.TAKEN -> {
                    StatusLabel("已按时服药", BrandGreen)
                    Spacer(Modifier.height(8.dp))
                    UndoButton(record, vm)
                }
                AdherenceRecord.LATE -> {
                    StatusLabel("已补服", WarmOrange)
                    Spacer(Modifier.height(8.dp))
                    UndoButton(record, vm)
                }
                AdherenceRecord.SKIPPED -> StatusLabel("已跳过", MaterialTheme.colorScheme.onSurfaceVariant)
                AdherenceRecord.SNOOZED -> StatusLabel("小睡中…", WarmOrange)
                AdherenceRecord.MISSED -> StatusLabel("漏服", ErrorRed)
                else -> ActionButtons(schedule, drug, vm)
            }
        }
    }
}

/** 未处理时的操作按钮：已服药 / 补服 / 跳过 */
@Composable
private fun ActionButtons(schedule: DoseSchedule, drug: Drug?, vm: MainViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row {
            Button(onClick = { drug?.let { vm.markTaken(schedule, it) } }) {
                Text("已服药")
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(onClick = { drug?.let { vm.markLate(schedule, it) } }) {
                Text("补服")
            }
        }
        TextButton(onClick = { drug?.let { vm.markSkip(schedule, it) } }) {
            Text("跳过本次", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** 撤销打卡按钮（仅打卡后 5 分钟内显示） */
@Composable
private fun UndoButton(record: AdherenceRecord, vm: MainViewModel) {
    val takenAt = record.takenAt ?: record.actualTime
    if (takenAt == null) return
    val withinWindow = System.currentTimeMillis() - takenAt <= 5 * 60 * 1000L
    if (!withinWindow) return
    var showConfirm by remember { androidx.compose.runtime.mutableStateOf(false) }
    TextButton(onClick = { showConfirm = true }) {
        Text("撤销打卡", color = ErrorRed)
    }
    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("撤销打卡") },
            text = { Text("撤销后将恢复为待处理状态，可在今日重新打卡。") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirm = false
                    vm.undoTake(record.id)
                }) { Text("撤销") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun StatusLabel(text: String, color: Color) {
    Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun DrugRow(drug: Drug, times: List<String>, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(drug.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        drug.owner,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .background(BrandGreen, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    "${drug.dosage} · ${times.joinToString("、")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "删除", tint = ErrorRed)
            }
        }
    }
}
