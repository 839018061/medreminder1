package com.example.medreminder.ui.guide

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.medreminder.ui.theme.BrandGreen
import com.example.medreminder.ui.theme.ErrorRed
import com.example.medreminder.ui.theme.WarmOrange
import com.example.medreminder.util.PermissionChecker
import com.example.medreminder.util.SystemSettingsNavigator
import com.example.medreminder.util.VendorSettings

private data class GuideItem(
    val key: String,
    val title: String,
    val desc: String,
    val granted: Boolean?, // null = 无法自动检测，需手动确认
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuideScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var refreshKey by remember { mutableStateOf(0) }

    // 从系统设置页返回后自动刷新各项状态
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshKey++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val items = remember(refreshKey) {
        buildList {
            add(
                GuideItem(
                    "notify", "通知权限",
                    "到点时弹出强提醒通知，锁屏也能看到",
                    PermissionChecker.isNotificationEnabled(context),
                    { SystemSettingsNavigator.openNotificationSettings(context) }
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(
                    GuideItem(
                        "alarm", "精确闹钟",
                        "允许在精确时间触发服药提醒，不延迟",
                        PermissionChecker.isExactAlarmEnabled(context),
                        { SystemSettingsNavigator.openExactAlarmSettings(context) }
                    )
                )
            }
            add(
                GuideItem(
                    "battery", "电池优化白名单",
                    "防止系统在后台清理应用导致提醒失效",
                    PermissionChecker.isIgnoringBatteryOptimizations(context),
                    { SystemSettingsNavigator.openBatteryOptimizationRequest(context) }
                )
            )
            add(
                GuideItem(
                    "autostart", "自启动 / 后台运行",
                    "小米、华为、OPPO、vivo 等需手动允许自启动",
                    null,
                    { VendorSettings.openAutoStart(context) }
                )
            )
        }
    }

    val done = items.count { it.granted == true }
    val total = items.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限与后台设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "为保证服药提醒不漏，建议全部开启",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "已开启 $done / $total 项",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (done == total) BrandGreen
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "点击每项可跳转到系统设置，设置完成后返回本页会自动刷新状态。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            items(items, key = { it.key }) { item ->
                GuideCard(item)
            }
        }
    }
}

@Composable
private fun GuideCard(item: GuideItem) {
    val (icon, tint, status) = when (item.granted) {
        true -> Triple(Icons.Filled.CheckCircle, BrandGreen, "已开启")
        false -> Triple(Icons.Filled.Cancel, ErrorRed, "未开启")
        null -> Triple(Icons.Filled.Info, WarmOrange, "需手动开启")
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    item.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(status, style = MaterialTheme.typography.labelLarge, color = tint, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            TextButton(onClick = item.onClick) {
                Text(if (item.granted == true) "查看" else "去开启")
            }
        }
    }
}
