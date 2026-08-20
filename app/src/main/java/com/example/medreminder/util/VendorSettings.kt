package com.example.medreminder.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * 跳转到各厂商的「自启动 / 后台运行」管理页。
 * 厂商未提供公开 API，这里用各厂商系统组件的隐式约定，失败时回退到应用详情页。
 */
object VendorSettings {

    fun openAutoStart(context: Context) {
        val manufacturer = Build.MANUFACTURER?.lowercase() ?: ""
        val intent = when {
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                component("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")
            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                component("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")
            manufacturer.contains("oppo") || manufacturer.contains("realme") ->
                component("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")
            manufacturer.contains("vivo") || manufacturer.contains("iqoo") ->
                component("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")
            manufacturer.contains("oneplus") ->
                component("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")
            manufacturer.contains("meizu") ->
                component("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity")
            else -> null
        }

        if (intent != null) {
            runCatching { context.startActivity(intent) }
                .onFailure { openAppDetails(context) }
        } else {
            openAppDetails(context)
        }
    }

    private fun component(pkg: String, cls: String): Intent =
        Intent().setComponent(ComponentName(pkg, cls)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    private fun openAppDetails(context: Context) {
        runCatching {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
