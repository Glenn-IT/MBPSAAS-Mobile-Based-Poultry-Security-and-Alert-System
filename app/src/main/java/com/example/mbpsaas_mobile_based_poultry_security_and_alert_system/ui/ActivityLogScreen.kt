package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.MotionEvent

@Composable
fun ActivityLogScreen(
    events: List<MotionEvent>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> CircularProgressIndicator(modifier = modifier.padding(24.dp))
        errorMessage != null -> Text(
            errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = modifier.padding(16.dp),
        )
        events.isEmpty() -> Text("No motion events logged yet.", modifier = modifier.padding(16.dp))
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(events) { event -> MotionEventRow(event) }
        }
    }
}
