package com.example.medreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.edit.RegimenEditScreen
import com.example.medreminder.ui.guide.PermissionGuideScreen
import com.example.medreminder.ui.home.HomeScreen
import com.example.medreminder.ui.importexport.ImportScreen
import com.example.medreminder.ui.stats.StatsScreen
import com.example.medreminder.ui.theme.MedReminderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MedReminderTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot(vm: MainViewModel = viewModel()) {
    val nav = rememberNavController()

    // 首次启动：若精确闹钟/通知权限有缺失，自动引导开启
    LaunchedEffect(Unit) {
        if (vm.shouldAutoShowPermissionGuide()) {
            vm.markPermissionGuideAutoShown()
            nav.navigate("permissions")
        }
    }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                vm = vm,
                onAdd = { nav.navigate("edit") },
                onStats = { nav.navigate("stats") },
                onImport = { nav.navigate("import") },
                onPermissions = { nav.navigate("permissions") }
            )
        }
        composable("permissions") {
            PermissionGuideScreen(onBack = { nav.popBackStack() })
        }
        composable("edit") {
            RegimenEditScreen(vm = vm, onBack = { nav.popBackStack() })
        }
        composable("stats") {
            StatsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
        composable("import") {
            ImportScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}
