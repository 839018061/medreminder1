package com.example.medreminder.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.layout.PaddingValues

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

    val today = DateUtils.today()
    val todaySchedules = schedules
        .filter { DateUtils.isScheduledToday(it.repeatDays) }
        .sortedBy { it.time }
    val todayRecordMap = records.filter { it.planDate == today }.associateBy { it.scheduleId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用药提醒", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onPermissions) {
                        Icon(Icons.Default.Settings, "权限与后台设置", tint = Color.White)
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "添加药品", tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
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

            item { SectionTitle("我的用药方案") }
            if (drugs.isEmpty()) {
                item { EmptyHint("还没有用药方案") }
            } else {
                items(drugs) { drug ->
                    DrugRow(
                        drug = drug,
                        times = schedules.filter { it.drugId == drug.id }.map { it.time },
                        onDelete = { vm.deleteDrug(drug) }
                    )
                }
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

@Composable
private fun DoseCard(
    schedule: DoseSchedule,
    drug: Drug?,
    record: AdherenceRecord?,
    vm: MainViewModel
) {
    val done = record != null && record.status != AdherenceRecord.MISSED
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
                    Text(drug?.name ?: "未知药品", style = MaterialTheme.typography.titleMedium)
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
            when {
                record?.status == AdherenceRecord.TAKEN -> StatusLabel("已按时服药", BrandGreen)
                record?.status == AdherenceRecord.LATE -> StatusLabel("已补服", WarmOrange)
                else -> {
                    Row {
                        Button(onClick = { drug?.let { vm.markTaken(schedule, it) } }) {
                            Text("已服药")
                        }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(onClick = { drug?.let { vm.markLate(schedule, it) } }) {
                            Text("补服")
                        }
                    }
                }
            }
        }
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
                Text(drug.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
