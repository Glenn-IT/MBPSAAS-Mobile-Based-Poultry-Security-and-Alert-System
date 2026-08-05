package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.ApiClient
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.User
import androidx.compose.runtime.LaunchedEffect

import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.SensorZone
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

private enum class HomeTab(val label: String) {
    Dashboard("Dashboard"),
    ActivityLog("Activity Log"),
    Profile("Profile"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User?,
    onLogout: () -> Unit,
    onProfileUpdated: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    var currentTab by remember { mutableStateOf(HomeTab.Dashboard) }
    var events by remember { mutableStateOf<List<MotionEvent>>(emptyList()) }
    var sensors by remember { mutableStateOf<List<SensorZone>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(refreshKey) {
        isLoading = true
        errorMessage = null
        try {
            val eventsResponse = ApiClient.service.getMotionEvents()
            if (eventsResponse.success) {
                events = eventsResponse.events ?: emptyList()
            } else {
                errorMessage = eventsResponse.message
            }

            val sensorsResponse = ApiClient.service.getSensors()
            if (sensorsResponse.success) {
                sensors = sensorsResponse.sensors ?: emptyList()
            }
        } catch (e: Exception) {
            errorMessage = "Cannot reach server: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val onToggleSensor: (String, Boolean) -> Unit = { sensorCode, newEnabledState ->
        // Optimistic UI update
        sensors = sensors.map {
            if (it.sensorCode == sensorCode) it.copy(isEnabled = newEnabledState) else it
        }
        coroutineScope.launch {
            try {
                val res = ApiClient.service.toggleSensor(sensorCode, if (newEnabledState) 1 else 0)
                if (res.success) {
                    // Refresh motion events so the new enable/disable log entry appears immediately
                    val eventsResponse = ApiClient.service.getMotionEvents()
                    if (eventsResponse.success) {
                        events = eventsResponse.events ?: emptyList()
                    }
                } else {
                    // Revert if request failed
                    sensors = sensors.map {
                        if (it.sensorCode == sensorCode) it.copy(isEnabled = !newEnabledState) else it
                    }
                }
            } catch (e: Exception) {
                // Revert on network failure
                sensors = sensors.map {
                    if (it.sensorCode == sensorCode) it.copy(isEnabled = !newEnabledState) else it
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Poultry Security System", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Hi, ${user?.username ?: "user"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { refreshKey++ }) {
                        Icon(Icons.Filled.NotificationsActive, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showLogoutConfirm = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == HomeTab.Dashboard,
                    onClick = { currentTab = HomeTab.Dashboard },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = null) },
                    label = { Text(HomeTab.Dashboard.label) },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.ActivityLog,
                    onClick = { currentTab = HomeTab.ActivityLog },
                    icon = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null) },
                    label = { Text(HomeTab.ActivityLog.label) },
                )
                NavigationBarItem(
                    selected = currentTab == HomeTab.Profile,
                    onClick = { currentTab = HomeTab.Profile },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(HomeTab.Profile.label) },
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (currentTab) {
            HomeTab.Dashboard -> DashboardScreen(
                events = events,
                sensors = sensors,
                isLoading = isLoading,
                errorMessage = errorMessage,
                onToggleSensor = onToggleSensor,
                modifier = contentModifier,
            )

            HomeTab.ActivityLog -> ActivityLogScreen(
                events = events,
                isLoading = isLoading,
                errorMessage = errorMessage,
                modifier = contentModifier,
            )

            HomeTab.Profile -> ProfileScreen(
                user = user,
                onLogout = { showLogoutConfirm = true },
                onProfileUpdated = onProfileUpdated,
                modifier = contentModifier,
            )
        }
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out?") },
            text = { Text("Are you sure you want to exit?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    onLogout()
                }) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}
