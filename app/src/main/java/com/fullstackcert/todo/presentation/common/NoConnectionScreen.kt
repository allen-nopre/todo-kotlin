package com.fullstackcert.todo.presentation.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fullstackcert.todo.R

@Composable
fun NoConnectionScreen(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_android_wifi_3_bar_off_24),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "No Internet Connection",
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                "Please check your connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

fun isNetworkError(message: String?) =
    message?.contains("Network error", ignoreCase = true) == true ||
    message?.contains("Unable to resolve host", ignoreCase = true) == true ||
    message?.contains("Failed to connect", ignoreCase = true) == true ||
    message?.contains("timeout", ignoreCase = true) == true
