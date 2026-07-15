package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.data.User
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui.DashboardScreen
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui.ForgotPasswordScreen
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui.LoginScreen
import com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui.theme.MBPSAASMobileBasedPoultrySecurityandAlertSystemTheme

private enum class Screen { Login, ForgotPassword, Home }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MBPSAASMobileBasedPoultrySecurityandAlertSystemTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    var screen by remember { mutableStateOf(Screen.Login) }
    var loggedInUser by remember { mutableStateOf<User?>(null) }

    when (screen) {
        Screen.Login -> LoginScreen(
            onLoginSuccess = { user ->
                loggedInUser = user
                screen = Screen.Home
            },
            onForgotPassword = { screen = Screen.ForgotPassword },
            modifier = modifier,
        )

        Screen.ForgotPassword -> ForgotPasswordScreen(
            onBackToLogin = { screen = Screen.Login },
            modifier = modifier,
        )

        Screen.Home -> DashboardScreen(
            user = loggedInUser,
            onLogout = {
                loggedInUser = null
                screen = Screen.Login
            },
            modifier = modifier,
        )
    }
}
