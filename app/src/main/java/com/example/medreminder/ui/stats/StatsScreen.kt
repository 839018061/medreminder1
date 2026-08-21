package com.example.medreminder.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medreminder.domain.AdherenceCalculator
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.theme.BrandGreen
import com.example.medreminder.ui.theme.ErrorRed
import com.example.medreminder.ui.theme.WarmOrange
import com.example.medreminder.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: MainViewModel, onBack: () -> Unit) {
    val records by vm.records.collectAsState()
    val adherenceByDrug by vm.adherenceByDrug.collectAsState()
    val today = DateUtils.today()

    val last7 = records.filter { it.planDate >= DateUtils.daysAgo(6) && it.planDate <= today }
    val last30 = records.filter { it.planDate >= DateUtils.daysAgo(29) && it.planDate <= today }

    val s7 = AdherenceCalculator.summarize(last7)
    val s30 = AdherenceCalculator.summarize(last30)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("依从性统计") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RateCard("近 7 天", s7)
            RateCard("近 30 天", s30)
            DrugRateCard(adherenceByDrug)
        }
    }
}

@Composable
private fun RateCard(title: String, s: AdherenceCalculator.Summary) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text(
                "依从率 ${(s.rate * 100).toInt()}%",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = BrandGreen
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatItem("总计划", s.total, Color.Gray)
                StatItem("按时", s.taken, BrandGreen)
                StatItem("补服", s.late, WarmOrange)
                StatItem("跳过", s.skipped, Color.Gray)
                StatItem("漏服", s.missed, ErrorRed)
            }
        }
    }
}

/** 按药品分组的依从率 */
@Composable
private fun DrugRateCard(items: List<com.example.medreminder.data.dao.DrugAdherence>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("按药品", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            if (items.isEmpty()) {
                Text(
                    "暂无统计数据",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                items.forEach { item ->
                    val finalized = item.taken + item.late + item.skipped + item.missed
                    val rate = if (finalized == 0) 1f
                    else (item.taken * 1f + item.late * 0.5f + item.skipped * 0.5f) / finalized
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                item.drugName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "总 ${item.total} · 按时 ${item.taken} · 补服 ${item.late} · 跳过 ${item.skipped} · 漏服 ${item.missed}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${(rate * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (rate >= 0.8f) BrandGreen else if (rate >= 0.5f) WarmOrange else ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, color: Color) {
    Column {
        Text("$value", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
