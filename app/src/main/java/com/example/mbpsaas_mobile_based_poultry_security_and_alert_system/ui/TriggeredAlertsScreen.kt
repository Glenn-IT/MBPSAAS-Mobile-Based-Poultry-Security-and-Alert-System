package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterOption(val label: String) {
    ALL("All Dates"),
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    CUSTOM("Custom Date")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriggeredAlertsScreen(
    events: List<MotionEvent>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selectedDateFilter by remember { mutableStateOf(DateFilterOption.ALL) }
    var selectedCustomDate by remember { mutableStateOf<String?>(null) } // YYYY-MM-DD format
    var selectedZoneFilter by remember { mutableStateOf<String?>(null) } // null = All Zones

    val calendar = Calendar.getInstance()
    val todayFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    val yesterdayCalendar = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val yesterdayFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCalendar.time)

    // DatePicker Dialog setup
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            selectedCustomDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
            selectedDateFilter = DateFilterOption.CUSTOM
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Filter only Triggered Motion Events (MOTION_DETECTED or buzzerTriggered)
    val triggeredAlerts = remember(events, selectedDateFilter, selectedCustomDate, selectedZoneFilter) {
        events.filter { event ->
            val isTriggered = event.eventType == "MOTION_DETECTED" || event.buzzerTriggered
            if (!isTriggered) return@filter false

            // Date filtering
            val eventDatePrefix = event.detectedAt.take(10) // Extracts YYYY-MM-DD
            val matchesDate = when (selectedDateFilter) {
                DateFilterOption.ALL -> true
                DateFilterOption.TODAY -> eventDatePrefix == todayFormatted
                DateFilterOption.YESTERDAY -> eventDatePrefix == yesterdayFormatted
                DateFilterOption.CUSTOM -> selectedCustomDate != null && eventDatePrefix == selectedCustomDate
            }
            if (!matchesDate) return@filter false

            // Zone filtering
            if (selectedZoneFilter != null && event.zone?.equals(selectedZoneFilter, ignoreCase = true) == false) {
                return@filter false
            }

            true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Title Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AlertColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Triggered Intrusion Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "${triggeredAlerts.size} alert logs recorded",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Date Filter Quick Chips
        Text(
            text = "Filter Alerts by Date",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(DateFilterOption.values()) { filterOption ->
                val isSelected = selectedDateFilter == filterOption
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (filterOption == DateFilterOption.CUSTOM) {
                            datePickerDialog.show()
                        } else {
                            selectedDateFilter = filterOption
                            selectedCustomDate = null
                        }
                    },
                    label = {
                        Text(
                            text = if (filterOption == DateFilterOption.CUSTOM && selectedCustomDate != null)
                                "Date: $selectedCustomDate" else filterOption.label
                        )
                    },
                    leadingIcon = {
                        if (filterOption == DateFilterOption.CUSTOM) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Zone Filter Chips (3 Coop Zones)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val zonesList = listOf(
                null to "All Zones",
                "ROOMA" to "Coop Zone A",
                "ROOMB" to "Coop Zone B",
                "ROOMC" to "Coop Zone C"
            )

            items(zonesList) { (zoneCode, zoneLabel) ->
                val isSelected = selectedZoneFilter == zoneCode
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedZoneFilter = zoneCode },
                    label = { Text(zoneLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Display Active Filter Summary & Clear Button
        if (selectedDateFilter != DateFilterOption.ALL || selectedZoneFilter != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Filter: ${if (selectedDateFilter == DateFilterOption.CUSTOM) selectedCustomDate else selectedDateFilter.label}" +
                            if (selectedZoneFilter != null) " (${getZoneDisplayName(selectedZoneFilter)})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        selectedDateFilter = DateFilterOption.ALL
                        selectedCustomDate = null
                        selectedZoneFilter = null
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Filter", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Alert Items List
        when {
            isLoading && events.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null && events.isEmpty() -> {
                Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            triggeredAlerts.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = SafeColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Triggered Alerts Found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "No intrusion alerts match your date and zone filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(triggeredAlerts) { alert ->
                        TriggeredAlertCard(alert = alert)
                    }
                }
            }
        }
    }
}

@Composable
private fun TriggeredAlertCard(alert: MotionEvent) {
    val zoneLabel = alert.zoneLabel ?: getZoneDisplayName(alert.zone)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AlertColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = AlertColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Intrusion Alert — $zoneLabel",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = AlertColor
                    )
                    Box(
                        modifier = Modifier
                            .background(AlertColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ALERT",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Detected at: ${alert.detectedAt}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Source: ${alert.source ?: "ARDUINO_PIR"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
