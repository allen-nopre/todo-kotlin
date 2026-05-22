package com.fullstackcert.todo.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fullstackcert.todo.R

private data class PasswordRule(val label: String, val passed: Boolean)

private fun evaluateRules(password: String, username: String): List<PasswordRule> {
    return listOf(
        PasswordRule(
            "Cannot contain your name",
            username.isBlank() || !password.contains(username, ignoreCase = true)
        ),
        PasswordRule("At least 8 characters", password.length >= 8),
        PasswordRule(
            "Contains a number or symbol",
            password.any { it.isDigit() || !it.isLetterOrDigit() }
        )
    )
}

private fun passwordStrength(password: String, username: String): Pair<String, Color> {
    val rules = evaluateRules(password, username)
    val passed = rules.count { it.passed }
    return when {
        password.isEmpty() -> "" to Color.Transparent
        passed <= 1 -> "Weak" to Color(0xFFF44336)
        passed == 2 -> "Fair" to Color(0xFFFF9800)
        password.length >= 12 -> "Strong" to Color(0xFF4CAF50)
        else -> "Good" to Color(0xFF8BC34A)
    }
}

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val rules = evaluateRules(password, username)
    val (strengthLabel, strengthColor) = passwordStrength(password, username)
    val allRulesPassed = rules.all { it.passed }

    LaunchedEffect(state.registrationSuccess) {
        if (state.registrationSuccess) {
            snackbarHostState.showSnackbar("Account created! Please sign in.")
            viewModel.resetRegistration()
            onNavigateBack()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Create an account", style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(32.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    viewModel.clearUsernameError()
                },
                label = { Text("Username") },
                singleLine = true,
                isError = state.usernameError != null,
                supportingText = {
                    if (state.usernameError != null) {
                        Text(state.usernameError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            // Password strength bar
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Password strength: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        strengthLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = strengthColor
                    )
                }
            }

            // Password rules
            if (password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rules.forEach { rule ->
                        PasswordRuleRow(label = rule.label, passed = rule.passed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(username, password) },
                enabled = !state.isLoading && username.isNotBlank() && allRulesPassed,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text("Create Account")
            }
            Spacer(modifier = Modifier.height(16.dp))

            val annotatedText = buildAnnotatedString {
                append("Already have an account? ")
                pushStringAnnotation(tag = "SIGNIN", annotation = "signin")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append("Sign in")
                }
                pop()
            }
            ClickableText(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                onClick = { offset ->
                    annotatedText.getStringAnnotations("SIGNIN", offset, offset)
                        .firstOrNull()?.let { onNavigateBack() }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Google")
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_facebook),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue with Facebook")
            }
        }
    }
}

@Composable
private fun PasswordRuleRow(label: String, passed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (passed) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(16.dp)
            )
        } else {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(6.dp)
            ) {}
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
