package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SensorsOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.SensorZone
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.ZoneStatus

internal val SafeColor = Color(0xFF2E7D32)
internal val AlertColor = Color(0xFFC62828)
internal val DisabledColor = Color(0xFF757575)
internal val InfoColor = Color(0xFF0288D1)

internal fun getZoneDisplayName(zoneCode: String?): String {
    return when (zoneCode?.uppercase()) {
        "ROOMA", "COOP1" -> "Coop Zone A"
        "ROOMB", "COOP2" -> "Coop Zone B"
        "ROOMC", "COOP3" -> "Coop Zone C"
        "ROOMD", "GATE", "PERIMETER" -> "Perimeter Gate"
        else -> zoneCode ?: "General Area"
    }
}

@Composable
internal fun MotionStatusCard(
    overallStatus: String?,
    latestEvent: MotionEvent?,
    modifier: Modifier = Modifier,
) {
    val isMotionDetected = overallStatus == "MOTION_DETECTED" || 
            latestEvent?.eventType == "MOTION_DETECTED" || 
            latestEvent?.buzzerTriggered == true

    val color = if (isMotionDetected) AlertColor else SafeColor
    val activeZone = latestEvent?.zoneLabel ?: getZoneDisplayName(latestEvent?.zone)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isMotionDetected) MaterialTheme.colorScheme.errorContainer 
                             else MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp),
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
                    imageVector = if (isMotionDetected) Icons.Filled.NotificationsActive else Icons.Filled.Shield,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp),
                )
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = if (isMotionDetected) "INTRUSION ALERT — $activeZone" else "ALL POULTRY ZONES SAFE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isMotionDetected) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = latestEvent?.detectedAt?.let { "Last activity: $it ($activeZone)" } ?: "System Online — Monitoring active",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMotionDetected) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) 
                            else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
internal fun ZoneStatusGrid(
    zones: Map<String, ZoneStatus>?,
    modifier: Modifier = Modifier,
) {
    if (zones.isNullOrEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Poultry Farm Zones",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            zones.entries.take(4).forEach { (code, zoneStatus) ->
                val isTriggered = zoneStatus.status == "MOTION_DETECTED" || zoneStatus.status == "MOTION"
                val badgeColor = if (isTriggered) AlertColor else SafeColor

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(badgeColor, CircleShape),
                            )
                            Icon(
                                imageVector = if (isTriggered) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = zoneStatus.label.ifBlank { getZoneDisplayName(code) },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                        )
                        Text(
                            text = if (isTriggered) "MOTION" else "Clear",
                            style = MaterialTheme.typography.labelSmall,
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
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
        shape = RoundedCornerShape(16.dp),
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
                    text = "PIR Sensors Control",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (sensors.isEmpty()) {
                Text(
                    "Loading PIR sensors status...",
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
                    text = if (sensor.isEnabled) "Sensor Active" else "Sensor Disabled",
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

@Composable
internal fun MotionEventRow(event: MotionEvent) {
    val isSystemLog = event.note?.contains("Enabled", ignoreCase = true) == true || 
                      event.note?.contains("Disabled", ignoreCase = true) == true

    val isMotionDetected = event.eventType == "MOTION_DETECTED" || event.buzzerTriggered

    val color = when {
        isSystemLog -> InfoColor
        isMotionDetected -> AlertColor
        else -> SafeColor
    }
    val zoneLabel = event.zoneLabel ?: getZoneDisplayName(event.zone)

    val title = when {
        isSystemLog -> event.note ?: "Sensor Status Changed ($zoneLabel)"
        isMotionDetected -> "Intrusion Alert ($zoneLabel)"
        else -> "Motion Stopped ($zoneLabel)"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape),
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    event.detectedAt, 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isSystemLog) {
                    event.note?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
