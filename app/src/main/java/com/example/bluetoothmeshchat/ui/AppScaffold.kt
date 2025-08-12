package com.example.bluetoothmeshchat.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bluetoothmeshchat.ui.screens.ChatScreen
import com.example.bluetoothmeshchat.ui.screens.PeersScreen
import com.example.bluetoothmeshchat.ui.screens.SettingsScreen

private sealed class Screen(val route: String, val label: String) {
    data object Chat : Screen("chat", "Chat")
    data object Peers : Screen("peers", "Peers")
    data object Settings : Screen("settings", "Settings")
}

@Composable
fun AppScaffold() {
    val navController = rememberNavController()
    val items = listOf(Screen.Chat, Screen.Peers, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Chat -> Icon(Icons.Default.Chat, contentDescription = null)
                                Screen.Peers -> Icon(Icons.Default.People, contentDescription = null)
                                Screen.Settings -> Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(screen.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController, startDestination = Screen.Chat.route, modifier = androidx.compose.ui.Modifier.padding(paddingValues)) {
            composable(Screen.Chat.route) { ChatScreen() }
            composable(Screen.Peers.route) { PeersScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}