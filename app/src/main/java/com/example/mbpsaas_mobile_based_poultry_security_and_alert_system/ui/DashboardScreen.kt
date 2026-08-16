package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.SensorZone
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.ZoneStatus

@Composable
fun DashboardScreen(
    overallStatus: String?,
    zones: Map<String, ZoneStatus>?,
    events: List<MotionEvent>,
    sensors: List<SensorZone>,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleSensor: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestEvent = events.firstOrNull()
    val recentEvents = events.take(10)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // 1. Overall Security Status Banner
        item {
            MotionStatusCard(
                overallStatus = overallStatus,
                latestEvent = latestEvent,
            )
        }

        // 2. Poultry Farm Zone Cards
        item {
            ZoneStatusGrid(
                zones = zones,
            )
        }

        // 3. Sensor Toggle Controls
        item {
            SensorControlCard(
                sensors = sensors,
                onToggleSensor = onToggleSensor,
            )
        }

        // 4. Section Title: Recent Intrusion Activity
        item {
            Text(
                text = "Recent Intrusion Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        // 5. Recent Activity List Items or Empty/Loading state
        when {
            isLoading && events.isEmpty() -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            errorMessage != null && events.isEmpty() -> {
                item {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }

            recentEvents.isEmpty() -> {
                item {
                    Text(
                        text = "No motion events logged yet. Farm is secure.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }

            else -> {
                items(recentEvents) { event ->
                    MotionEventRow(event)
                }
            }
        }
    }
}
