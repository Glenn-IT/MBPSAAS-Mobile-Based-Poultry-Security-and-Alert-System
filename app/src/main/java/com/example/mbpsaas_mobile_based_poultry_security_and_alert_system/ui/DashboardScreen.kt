package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent

import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.SensorZone

@Composable
fun DashboardScreen(
    events: List<MotionEvent>,
    sensors: List<SensorZone>,
    isLoading: Boolean,
    errorMessage: String?,
    onToggleSensor: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val latestEvent = events.firstOrNull()
    val recentEvents = events.take(3)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MotionStatusCard(latestEvent)

        SensorControlCard(
            sensors = sensors,
            onToggleSensor = onToggleSensor,
        )

        Text(
            "Recent activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
            errorMessage != null -> Text(errorMessage, color = MaterialTheme.colorScheme.error)
            recentEvents.isEmpty() -> Text("No motion events logged yet.")
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(recentEvents) { event -> MotionEventRow(event) }
            }
        }
    }
}
