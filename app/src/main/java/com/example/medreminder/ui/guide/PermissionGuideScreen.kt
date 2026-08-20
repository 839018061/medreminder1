package com.example.medreminder.ui.guide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.medreminder.util.PermissionChecker
import com.example.medreminder.util.SystemSettingsNavigator
import com.example.medreminder.util.VendorSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionGuideScreen(onBack: () -> Unit) {
    var refreshTick by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshTick++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val ctx = androidx.compose.ui.platform.LocalContext.current
    val notif = PermissionChecker.notificationGranted(ctx)
    val alarm = PermissionChecker.exactAlarmGranted(ctx)
    val battery = PermissionChecker.batteryOptimizationExempt(ctx)
    val grantedCount = listOf(notif, alarm, battery).count { it }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限与后台设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        progress = grantedCount / 3f,
                        modifier = Modifier.size(56.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("已开启 ${grantedCount}/3", style = MaterialTheme.typography.titleMedium)
                        Text("开启全部权限，锁屏也能准时提醒", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            PermissionItem(
                icon = { Icon(Icons.Default.Notifications, null, tint = Color.White) },
                title = "通知权限",
                desc = "在屏幕关闭或锁屏时显示服药提醒通知",
                granted = notif,
                onAction = { SystemSettingsNavigator.openAppNotificationSettings(ctx) }
            )
            PermissionItem(
                icon = { Icon(Icons.Default.Alarm, null, tint = Color.White) },
                title = "精确闹钟",
                desc = "确保提醒在设定时间准时响起，不受系统限制",
                granted = alarm,
                onAction = { SystemSettingsNavigator.openAlarmPermissionSettings(ctx) }
            )
            PermissionItem(
                icon = { Icon(Icons.Default.BatteryFull, null, tint = Color.White) },
                title = "电池白名单",
                desc = "避免系统清理后台导致提醒被延迟",
                granted = battery,
                onAction = { SystemSettingsNavigator.openBatteryOptimizationRequest(ctx) }
            )
            PermissionItem(
                icon = { Icon(Icons.Default.PhoneAndroid, null, tint = Color.White) },
                title = "自启动（${VendorSettings.vendorLabel}）",
                desc = "国产手机需手动开启自启动，防止锁屏后提醒失效",
                granted = false,
                autoGrantedNote = "需手动开启",
                onAction = { VendorSettings.openAutoStartSettings(ctx) }
            )
        }
    }
}

@Composable
private fun PermissionItem(
    icon: @Composable () -> Unit,
    title: String,
    desc: String,
    granted: Boolean,
    onAction: () -> Unit,
    autoGrantedNote: String? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.errorContainer
                ) {
                    Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { icon() }
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (granted) {
                Text("已开启", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            } else if (autoGrantedNote != null) {
                Text(autoGrantedNote, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Button(onClick = onAction) { Text("去开启") }
            } else {
                Button(onClick = onAction) { Text("去开启") }
            }
        }
    }
}
