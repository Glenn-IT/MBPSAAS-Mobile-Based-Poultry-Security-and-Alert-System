package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.SensorZone

internal val SafeColor = Color(0xFF2E7D32)
internal val AlertColor = Color(0xFFC62828)
internal val DisabledColor = Color(0xFF757575)

internal fun getZoneDisplayName(zoneCode: String?): String {
    return when (zoneCode?.uppercase()) {
        "COOP1" -> "Coop 1"
        "COOP2" -> "Coop 2"
        "PERIMETER" -> "Perimeter"
        else -> zoneCode ?: "General Area"
    }
}

@Composable
internal fun MotionStatusCard(latestEvent: MotionEvent?) {
    val triggered = latestEvent?.buzzerTriggered == true
    val color = if (triggered) AlertColor else SafeColor
    val zoneLabel = getZoneDisplayName(latestEvent?.zone)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (triggered) Icons.Filled.NotificationsActive else Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    if (triggered) "Intruder Alert — $zoneLabel" else "No motion — coop is secure",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    latestEvent?.detectedAt?.let { "Last event: $it ($zoneLabel)" } ?: "No events logged yet",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
internal fun SensorControlCard(
    sensors: List<SensorZone>,
    onToggleSensor: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PIR Sensors Enable/Disable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (sensors.isEmpty()) {
                Text(
                    "Loading PIR sensors...",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                sensors.forEachIndexed { index, sensor ->
                    SensorZoneItem(
                        sensor = sensor,
                        onToggle = { isEnabled -> onToggleSensor(sensor.sensorCode, isEnabled) }
                    )
                    if (index < sensors.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensorZoneItem(
    sensor: SensorZone,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = if (sensor.isEnabled) Icons.Filled.Sensors else Icons.Filled.SensorsOff,
                contentDescription = null,
                tint = if (sensor.isEnabled) SafeColor else DisabledColor,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = sensor.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (sensor.isEnabled) "PIR Sensor Enabled" else "PIR Sensor Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sensor.isEnabled) SafeColor else DisabledColor,
                )
            }
        }

        Switch(
            checked = sensor.isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SafeColor,
            ),
        )
    }
}

internal val InfoColor = Color(0xFF0288D1)

@Composable
internal fun MotionEventRow(event: MotionEvent) {
    val isSystemLog = event.note?.contains("Enabled", ignoreCase = true) == true || 
                      event.note?.contains("Disabled", ignoreCase = true) == true

    val color = when {
        isSystemLog -> InfoColor
        event.buzzerTriggered -> AlertColor
        else -> SafeColor
    }
    val zoneLabel = getZoneDisplayName(event.zone)

    val title = when {
        isSystemLog -> event.note ?: "Sensor Status Changed ($zoneLabel)"
        event.buzzerTriggered -> "Intruder Alert ($zoneLabel) — buzzer sounded"
        else -> "Motion Alert ($zoneLabel)"
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(event.detectedAt, style = MaterialTheme.typography.bodySmall)
                if (!isSystemLog) {
                    event.note?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
