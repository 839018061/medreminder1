package com.example.medreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medreminder.ui.MainViewModel
import com.example.medreminder.ui.edit.RegimenEditScreen
import com.example.medreminder.ui.guide.PermissionGuideScreen
import com.example.medreminder.ui.home.HomeScreen
import com.example.medreminder.ui.importexport.ImportScreen
import com.example.medreminder.ui.stats.StatsScreen
import com.example.medreminder.ui.theme.MedReminderTheme
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedReminderTheme {
                val navController = rememberNavController()
                val vm: MainViewModel = viewModel()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            vm = vm,
                            onAdd = { navController.navigate("edit") },
                            onStats = { navController.navigate("stats") },
                            onImport = { navController.navigate("import") },
                            onPermissions = { navController.navigate("permissions") }
                        )
                    }
                    composable("permissions") {
                        PermissionGuideScreen(onBack = { navController.popBackStack() })
                    }
                    composable("edit") {
                        RegimenEditScreen(vm = vm, onBack = { navController.popBackStack() })
                    }
                    composable("stats") {
                        StatsScreen(vm = vm, onBack = { navController.popBackStack() })
                    }
                    composable("import") {
                        ImportScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}
