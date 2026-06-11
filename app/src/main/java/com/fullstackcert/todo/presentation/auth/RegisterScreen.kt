package com.fullstackcert.todo.presentation.auth

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.fullstackcert.todo.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

private data class PasswordRule(val label: String, val passed: Boolean)

private val ALLOWED_USERNAME_REGEX = Regex("^[a-zA-Z0-9 !#()_-]*$")

private fun isValidUsername(username: String) = ALLOWED_USERNAME_REGEX.matches(username)


private fun evaluateRules(password: String, username: String): List<PasswordRule> {
    return listOf(
        PasswordRule("rule_no_name", username.isBlank() || !password.contains(username, ignoreCase = true)),
        PasswordRule("rule_min_length", password.length >= 8),
        PasswordRule("rule_number_or_symbol", password.any { it.isDigit() || !it.isLetterOrDigit() })
    )
}

private fun passwordStrength(password: String, username: String): Pair<Int, Color> {
    val rules = evaluateRules(password, username)
    val passed = rules.count { it.passed }
    return when {
        password.isEmpty() -> R.string.app_name to Color.Transparent
        passed <= 1 -> R.string.strength_weak to Color(0xFFF44336)
        passed == 2 -> R.string.strength_fair to Color(0xFFFF9800)
        password.length >= 12 -> R.string.strength_strong to Color(0xFF4CAF50)
        else -> R.string.strength_good to Color(0xFF8BC34A)
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
    var usernameSymbolError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val callbackManager = remember { CallbackManager.Factory.create() }
    DisposableEffect(Unit) {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.loginWithFacebook(result.accessToken.token)
                }
                override fun onCancel() {}
                override fun onError(error: FacebookException) {}
            }
        )
        onDispose { LoginManager.getInstance().unregisterCallback(callbackManager) }
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) onNavigateBack()
    }

    val rules = evaluateRules(password, username)
    val (strengthLabelRes, strengthColor) = passwordStrength(password, username)
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
            Text(stringResource(R.string.create_an_account), style = MaterialTheme.typography.displaySmall)
            Spacer(modifier = Modifier.height(32.dp))

            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (isValidUsername(it)) {
                        username = it
                        usernameSymbolError = false
                    } else {
                        usernameSymbolError = true
                    }
                    viewModel.clearUsernameError()
                },
                label = { Text(stringResource(R.string.username)) },
                singleLine = true,
                isError = state.usernameError != null || usernameSymbolError,
                supportingText = {
                    when {
                        usernameSymbolError -> Text(
                            "Allowed: letters, numbers, spaces, ! # ( ) _ -",
                            color = MaterialTheme.colorScheme.error
                        )
                        state.usernameError != null -> Text(
                            state.usernameError!!,
                            color = MaterialTheme.colorScheme.error
                        )
                        else -> Text(
                            "Allowed: letters, numbers, spaces, ! # ( ) _ -",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (isValidUsername(it)) password = it
                },
                label = { Text(stringResource(R.string.password)) },
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
                        stringResource(R.string.password_strength),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        stringResource(strengthLabelRes),
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
                        PasswordRuleRow(
                            labelRes = when (rule.label) {
                                "rule_no_name" -> R.string.rule_no_name
                                "rule_min_length" -> R.string.rule_min_length
                                else -> R.string.rule_number_or_symbol
                            },
                            passed = rule.passed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(username, password) },
                enabled = !state.isLoading && username.isNotBlank() && allRulesPassed && !usernameSymbolError,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else Text(stringResource(R.string.create_account_btn))
            }
            Spacer(modifier = Modifier.height(16.dp))

            val annotatedText = buildAnnotatedString {
                append(stringResource(R.string.already_have_account))
                pushStringAnnotation(tag = "SIGNIN", annotation = "signin")
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.sign_in_link))
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
                    text = stringResource(R.string.or_divider),
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(true)
                                .setServerClientId(context.getString(R.string.google_web_client_id))
                                .setAutoSelectEnabled(true)
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val credResult = credentialManager.getCredential(
                                request = request,
                                context = context as ComponentActivity
                            )
                            val credential = credResult.credential
                            if (credential is CustomCredential &&
                                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                            ) {
                                val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                viewModel.loginWithGoogle(idToken)
                            }
                        } catch (e: GetCredentialException) {
                            try {
                                val signInOption = GetSignInWithGoogleOption.Builder(
                                    context.getString(R.string.google_web_client_id)
                                ).build()
                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(signInOption)
                                    .build()
                                val credResult = credentialManager.getCredential(
                                    request = request,
                                    context = context as ComponentActivity
                                )
                                val credential = credResult.credential
                                if (credential is CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                                ) {
                                    val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                                    viewModel.loginWithGoogle(idToken)
                                }
                            } catch (e2: GetCredentialException) {
                                snackbarHostState.showSnackbar("Google sign-in failed: ${e2.message}")
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.continue_with_google))
            }
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    LoginManager.getInstance().logInWithReadPermissions(
                        context as ComponentActivity,
                        callbackManager,
                        listOf("email", "public_profile")
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_facebook),
                    contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color.Unspecified,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.continue_with_facebook))
            }
        }
    }
}

@Composable
private fun PasswordRuleRow(labelRes: Int, passed: Boolean) {
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
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodySmall,
            color = if (passed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
