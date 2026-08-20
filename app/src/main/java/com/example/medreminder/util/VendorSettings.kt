package com.example.medreminder.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

/** 国产厂商系统「自启动/后台管理」设置页跳转 */
object VendorSettings {
    private val vendor = Build.MANUFACTURER.lowercase()

    fun isXiaomi() = vendor.contains("xiaomi") || vendor.contains("redmi")
    fun isHuawei() = vendor.contains("huawei") || vendor.contains("honor")
    fun isOppo() = vendor.contains("oppo") || vendor.contains("realme")
    fun isVivo() = vendor.contains("vivo") || vendor.contains("iqoo")
    fun isOnePlus() = vendor.contains("oneplus")
    fun isMeizu() = vendor.contains("meizu")

    val vendorLabel: String = when {
        isXiaomi() -> "小米/红米"
        isHuawei() -> "华为/荣耀"
        isOppo() -> "OPPO/realme"
        isVivo() -> "vivo/iQOO"
        isOnePlus() -> "一加"
        isMeizu() -> "魅族"
        else -> "其他"
    }

    /** 尝试打开厂商「自启动管理」页，失败则回退到应用详情页 */
    fun openAutoStartSettings(context: Context) {
        val candidates = autoStartIntents()
        var opened = false
        for (intent in candidates) {
            try {
                context.startActivity(intent)
                opened = true
                break
            } catch (_: Exception) {
                continue
            }
        }
        if (!opened) {
            openAppDetails(context)
        }
    }

    private fun autoStartIntents(): List<Intent> {
        val base = listOf(
            // 小米
            Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            // 华为
            Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            // 荣耀
            Intent().setComponent(ComponentName("com.hihonor.systemmanager", "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
            // OPPO
            Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            // vivo
            Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            // 一加
            Intent().setComponent(ComponentName("com.oneplus.security", "com.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity"))
        )
        return base
    }

    fun openAppDetails(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
