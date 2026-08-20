package com.example.medreminder.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.theme.ErrorRed

/** 单个服药时间点的草稿（时间 + 服用时机 + 重复规则） */
private data class TimePointDraft(
    val time: String = "08:00",
    val relation: String = "饭前",
    val useDaily: Boolean = true,
    val weekDays: Set<Int> = emptySet()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RegimenEditScreen(vm: MainViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var remark by remember { mutableStateOf("") }
    val timePoints = remember { mutableStateListOf(TimePointDraft()) }

    val relations = listOf("饭前", "饭后", "睡前", "无")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加药品") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("药品名 *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("单次剂量 *（如 1片 / 5ml）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("备注（可选）") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text("服药时间", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            timePoints.forEachIndexed { index, tp ->
                TimePointCard(
                    draft = tp,
                    relations = relations,
                    canRemove = timePoints.size > 1,
                    onChange = { timePoints[index] = it },
                    onRemove = { timePoints.removeAt(index) }
                )
            }
            OutlinedButton(
                onClick = { timePoints.add(TimePointDraft()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Text(" 添加服药时间点")
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val validTime = timePoints.all { it.time.matches(Regex("\\d{1,2}:\\d{2}")) }
                    if (name.isBlank() || dosage.isBlank() || !validTime) return@Button
                    val drug = Drug(name = name.trim(), dosage = dosage.trim(), remark = remark.trim())
                    val schedules = timePoints.map { tp ->
                        val rd = if (tp.useDaily || tp.weekDays.isEmpty()) "daily"
                        else tp.weekDays.sorted().joinToString(",")
                        DoseSchedule(time = tp.time.trim(), repeatDays = rd, relation = tp.relation)
                    }
                    vm.addDrug(drug, schedules)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("保存并开始提醒", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun TimePointCard(
    draft: TimePointDraft,
    relations: List<String>,
    canRemove: Boolean,
    onChange: (TimePointDraft) -> Unit,
    onRemove: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "时间点",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, "删除该时间点", tint = ErrorRed)
                    }
                }
            }
            OutlinedTextField(
                value = draft.time,
                onValueChange = { onChange(draft.copy(time = it)) },
                label = { Text("HH:mm，如 08:00") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text("服用时机", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                relations.forEach { r ->
                    FilterChip(
                        selected = draft.relation == r,
                        onClick = { onChange(draft.copy(relation = r)) },
                        label = { Text(r) }
                    )
                }
            }
            Text("重复", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = draft.useDaily,
                    onClick = { onChange(draft.copy(useDaily = true)) },
                    label = { Text("每天") }
                )
                (1..7).forEach { d ->
                    FilterChip(
                        selected = !draft.useDaily && draft.weekDays.contains(d),
                        onClick = {
                            val cur = draft.weekDays
                            onChange(
                                draft.copy(
                                    useDaily = false,
                                    weekDays = if (d in cur) cur - d else cur + d
                                )
                            )
                        },
                        label = { Text("周${weekName(d)}") }
                    )
                }
            }
        }
    }
}

private fun weekName(d: Int): String = when (d) {
    1 -> "一"; 2 -> "二"; 3 -> "三"; 4 -> "四"; 5 -> "五"; 6 -> "六"; else -> "日"
}
