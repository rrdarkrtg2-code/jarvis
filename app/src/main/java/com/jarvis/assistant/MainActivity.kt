package com.jarvis.assistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jarvis.assistant.ui.screens.ChatScreen
import com.jarvis.assistant.ui.screens.DiagnosticsScreen
import com.jarvis.assistant.ui.screens.HomeScreen
import com.jarvis.assistant.ui.screens.MemoryScreen
import com.jarvis.assistant.ui.screens.RemindersScreen
import com.jarvis.assistant.ui.screens.SettingsScreen
import com.jarvis.assistant.ui.screens.ToolsScreen
import com.jarvis.assistant.ui.theme.JARVISTheme
import com.jarvis.assistant.ui.theme.JarvisBackground
import com.jarvis.assistant.ui.theme.JarvisCard
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisTextPrimary
import com.jarvis.assistant.ui.theme.JarvisTextSecondary
import com.jarvis.assistant.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Permissions granted status callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestAppPermissions()

        setContent {
            JARVISTheme {
                MainAppNav(viewModel)
            }
        }
    }

    private fun requestAppPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty()) {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Chat : Screen("chat", "Chat", Icons.Default.Chat)
    object Tools : Screen("tools", "Tools", Icons.Default.Widgets)
    object Memory : Screen("memory", "Memory", Icons.Default.Psychology)
    object Reminders : Screen("reminders", "Alerts", Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Diagnostics : Screen("diagnostics", "Diagnostics", Icons.Default.MoreHoriz)
}

@Composable
fun MainAppNav(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavScreens = listOf(
        Screen.Home,
        Screen.Chat,
        Screen.Tools,
        Screen.Memory,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(JarvisBackground),
        bottomBar = {
            NavigationBar(
                containerColor = JarvisCard,
                contentColor = JarvisCyan
            ) {
                bottomNavScreens.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JarvisBackground,
                            selectedTextColor = JarvisCyan,
                            indicatorColor = JarvisCyan,
                            unselectedIconColor = JarvisTextSecondary,
                            unselectedTextColor = JarvisTextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChat = { navController.navigate(Screen.Chat.route) },
                    onNavigateToTools = { navController.navigate(Screen.Tools.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Chat.route) {
                ChatScreen(viewModel = viewModel)
            }
            composable(Screen.Tools.route) {
                ToolsScreen(viewModel = viewModel)
            }
            composable(Screen.Memory.route) {
                MemoryScreen()
            }
            composable(Screen.Reminders.route) {
                RemindersScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) })
            }
            composable(Screen.Diagnostics.route) {
                DiagnosticsScreen()
            }
        }
    }
}
