package com.example.mbpsaas_mobile_based_poultry_security_and_alert_system.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import kotlinx.coroutines.launch

private enum class ResetStep { EnterUsername, AnswerQuestion, NewPassword }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by remember { mutableStateOf(ResetStep.EnterUsername) }
    var username by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedQuestion by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var answer by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { onBackToLogin() },
            title = { Text("Password Updated") },
            text = { Text("Your password has been reset successfully. You can now log in with your new password.") },
            confirmButton = {
                TextButton(onClick = { onBackToLogin() }) {
                    Text("OK")
                }
            },
        )
    }

    fun runCall(block: suspend () -> Unit) {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                block()
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
        Text("Forgot Password", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            when (step) {
                ResetStep.EnterUsername -> "Step 1 of 3: Who are you?"
                ResetStep.AnswerQuestion -> "Step 2 of 3: Pick your security question and answer it"
                ResetStep.NewPassword -> "Step 3 of 3: Choose a new password"
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(24.dp))

        when (step) {
            ResetStep.EnterUsername -> {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ResetStep.AnswerQuestion -> {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedQuestion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Security Question") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        questions.forEach { question ->
                            DropdownMenuItem(
                                text = { Text(question) },
                                onClick = {
                                    selectedQuestion = question
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    label = { Text("Your Answer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            ResetStep.NewPassword -> {
                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New Password",
                )
                Spacer(Modifier.height(12.dp))
                PasswordField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                )
            }
        }

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                when (step) {
                    ResetStep.EnterUsername -> {
                        if (username.isBlank()) {
                            errorMessage = "Please enter your username or email"
                        } else runCall {
                            val response = ApiClient.service.checkUser(username.trim())
                            if (!response.success) {
                                errorMessage = response.message
                                return@runCall
                            }
                            val questionsResponse = ApiClient.service.getQuestions()
                            if (questionsResponse.success && !questionsResponse.questions.isNullOrEmpty()) {
                                questions = questionsResponse.questions
                                step = ResetStep.AnswerQuestion
                            } else {
                                errorMessage = questionsResponse.message
                            }
                        }
                    }

                    ResetStep.AnswerQuestion -> {
                        if (selectedQuestion.isBlank()) {
                            errorMessage = "Please pick your security question"
                        } else if (answer.isBlank()) {
                            errorMessage = "Please enter your answer"
                        } else runCall {
                            val response = ApiClient.service.verifySecurityAnswer(
                                username.trim(), selectedQuestion, answer.trim(),
                            )
                            if (response.success && response.resetToken != null) {
                                resetToken = response.resetToken
                                step = ResetStep.NewPassword
                            } else {
                                errorMessage = response.message
                            }
                        }
                    }

                    ResetStep.NewPassword -> {
                        if (newPassword.length < 6) {
                            errorMessage = "Password must be at least 6 characters"
                        } else if (newPassword != confirmPassword) {
                            errorMessage = "Passwords do not match"
                        } else runCall {
                            val response = ApiClient.service.resetPassword(
                                username.trim(), resetToken, newPassword,
                            )
                            if (response.success) {
                                showSuccessDialog = true
                            } else {
                                errorMessage = response.message
                            }
                        }
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp))
            } else {
                Text(
                    when (step) {
                        ResetStep.EnterUsername -> "Next"
                        ResetStep.AnswerQuestion -> "Verify Answer"
                        ResetStep.NewPassword -> "Reset Password"
                    }
                )
            }
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to Login")
        }
    }
}
