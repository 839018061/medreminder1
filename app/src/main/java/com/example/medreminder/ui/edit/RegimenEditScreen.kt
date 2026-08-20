package com.example.medreminder.ui.edit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medreminder.data.entity.DoseSchedule
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegimenEditScreen(vm: MainViewModel, onBack: () -> Unit) {
    var drugName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    // 每个时间点：时间 / 时机 / 周几重复
    data class TimePoint(
        var time: String = "08:00",
        var timing: String = "早餐后",
        var repeatDays: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7)
    )
    var timePoints by remember { mutableStateOf(listOf(TimePoint())) }

    val timings = listOf("早餐前", "早餐后", "午餐前", "午餐后", "晚餐前", "晚餐后", "睡前")
    val weekdays = listOf(1 to "一", 2 to "二", 3 to "三", 4 to "四", 5 to "五", 6 to "六", 7 to "日")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加用药方案") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    if (drugName.isNotBlank()) {
                        val drug = Drug(name = drugName.trim(), dosage = dosage.trim(), note = note.trim())
                        val schedules = timePoints.map { tp ->
                            DoseSchedule(time = tp.time, timing = tp.timing, repeatDays = tp.repeatDays)
                        }
                        vm.saveDrug(drug, schedules)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("保存")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = drugName, onValueChange = { drugName = it },
                label = { Text("药品名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dosage, onValueChange = { dosage = it },
                label = { Text("剂量（可选，如 1片/50mg）") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("备注（可选）") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("服药时间点", style = MaterialTheme.typography.titleMedium)
            timePoints.forEachIndexed { index, tp ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("时间点 ${index + 1}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            if (timePoints.size > 1) {
                                IconButton(onClick = { timePoints = timePoints.filterIndexed { i, _ -> i != index } }) {
                                    Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = tp.time, onValueChange = { tp.time = it },
                            label = { Text("时间 (HH:mm)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        var timing by remember { mutableStateOf(tp.timing) }
                        Text("服用时机", style = MaterialTheme.typography.labelLarge)
                        FlowRow {
                            timings.forEach { t ->
                                FilterChip(
                                    selected = timing == t,
                                    onClick = { timing = t; tp.timing = t },
                                    label = { Text(t) }
                                )
                            }
                        }
                        Text("重复", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            weekdays.forEach { (day, label) ->
                                val selected = tp.repeatDays.contains(day)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        tp.repeatDays = if (selected) tp.repeatDays - day else tp.repeatDays + day
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { timePoints = timePoints + TimePoint() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(4.dp))
                Text("添加服药时间点")
            }
        }
    }
}

@Composable
fun FlowRow(content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) { content() }
}
