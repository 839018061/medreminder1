package com.example.medreminder.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.medreminder.data.entity.Drug
import com.example.medreminder.ui.MainViewModel

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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("用药提醒") },
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
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, "添加药品")
            }
        }
    ) { padding ->
        if (drugs.isEmpty()) {
            EmptyState(Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(drugs, key = { it.id }) { drug ->
                    DrugCard(drug, onClick = onStats)
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Medication, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text("还没有用药方案", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("点击右下角 + 添加第一种药", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DrugCard(drug: Drug, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Medication,
                null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(drug.name, style = MaterialTheme.typography.titleMedium)
                if (drug.dosage.isNotBlank()) {
                    Text("剂量：${drug.dosage}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
