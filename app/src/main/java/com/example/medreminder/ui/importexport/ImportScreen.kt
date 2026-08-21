package com.example.medreminder.ui.importexport

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import android.content.Intent
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.medreminder.importexport.RegimenCodec
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.theme.BrandGreen
import com.example.medreminder.ui.theme.ErrorRed
import com.example.medreminder.util.PdfExporter
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(vm: MainViewModel, onBack: () -> Unit) {
    var jsonText by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val drugs by vm.drugs.collectAsState()
    val schedules by vm.schedules.collectAsState()

    val scanLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { jsonText = it }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导入用药方案") },
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
            Text(
                "由家属在手机/电脑上生成用药方案二维码，老人扫码即可一键导入。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    scanLauncher.launch(
                        ScanOptions()
                            .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            .setPrompt("扫描用药方案二维码")
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.QrCodeScanner, null)
                Spacer(Modifier.height(0.dp))
                Text(" 扫码导入")
            }
            OutlinedTextField(
                value = jsonText,
                onValueChange = { jsonText = it },
                label = { Text("或粘贴方案内容") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)
            )
            Button(
                onClick = {
                    msg = try {
                        val regimen = RegimenCodec.decode(jsonText.trim())
                        vm.importRegimen(regimen)
                        "导入成功"
                    } catch (e: Exception) {
                        "导入失败：${e.message}"
                    }
                },
                enabled = jsonText.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("导入")
            }
            msg?.let {
                Text(
                    it,
                    color = if (it == "导入成功") BrandGreen else ErrorRed,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(8.dp))
            Text("导出用药方案（分享给家属）", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    val uri = PdfExporter.export(context, drugs, schedules)
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "分享用药方案"))
                    } else {
                        msg = "导出失败"
                    }
                },
                enabled = drugs.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Text(" 导出 PDF 并分享")
            }
        }
    }
}
