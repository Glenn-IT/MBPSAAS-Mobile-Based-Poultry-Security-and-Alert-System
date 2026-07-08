package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.ApiClient
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.User
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    onForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Please enter username and password"
            return
        }
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = ApiClient.service.login(username.trim(), password)
                if (response.success && response.user != null) {
                    onLoginSuccess(response.user)
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Cannot reach server: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("MBPSAAS", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Poultry Security & Alert System",
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username or Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
        )

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { submit() },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text("Login")
            }
        }

        TextButton(onClick = onForgotPassword) {
            Text("Forgot Password?")
        }
    }
}
