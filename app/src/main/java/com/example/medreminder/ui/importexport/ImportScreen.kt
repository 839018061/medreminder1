package com.example.medreminder.ui.importexport

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("导入方案") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
        }) }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            Text("扫码导入：使用二维码分享用药方案。", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
