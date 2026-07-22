package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.ApiClient
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.User
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    user: User?,
    onLogout: () -> Unit,
    onProfileUpdated: (User) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }

    var username by remember(user, isEditing) { mutableStateOf(user?.username ?: "") }
    var email by remember(user, isEditing) { mutableStateOf(user?.email ?: "") }
    var currentPassword by remember(isEditing) { mutableStateOf("") }
    var newPassword by remember(isEditing) { mutableStateOf("") }

    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun startEditing() {
        isEditing = true
        errorMessage = null
        successMessage = null
    }

    fun cancelEditing() {
        isEditing = false
        errorMessage = null
    }

    fun submit() {
        if (user == null) return
        if (username.isBlank() || email.isBlank()) {
            errorMessage = "Username and email cannot be empty"
            return
        }
        if (currentPassword.isBlank()) {
            errorMessage = "Enter your current password to confirm changes"
            return
        }
        scope.launch {
            isSaving = true
            errorMessage = null
            try {
                val response = ApiClient.service.updateProfile(
                    userId = user.id,
                    currentPassword = currentPassword,
                    newUsername = username.trim(),
                    newEmail = email.trim(),
                    newPassword = newPassword,
                )
                if (response.success && response.user != null) {
                    onProfileUpdated(response.user)
                    successMessage = "Profile updated successfully"
                    isEditing = false
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Cannot reach server: ${e.message}"
            } finally {
                isSaving = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Text(
                    user?.username ?: "user",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    user?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        if (isEditing) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        "Edit credentials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    PasswordField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New password (leave blank to keep current)",
                    )
                    PasswordField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = "Current password (required to confirm)",
                    )

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { cancelEditing() },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Cancel")
                        }
                        Spacer(Modifier.width(12.dp))
                        Button(
                            onClick = { submit() },
                            enabled = !isSaving,
                            modifier = Modifier.weight(1f),
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Text("Save")
                            }
                        }
                    }
                }
            }
        } else {
            successMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.primary)
            }
            OutlinedButton(onClick = { startEditing() }, modifier = Modifier.fillMaxWidth()) {
                Text("Update credentials")
            }
        }

        Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Text(" Log out", modifier = Modifier.padding(start = 8.dp))
        }
    }
}
