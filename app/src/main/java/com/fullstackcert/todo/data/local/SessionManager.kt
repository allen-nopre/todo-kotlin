package com.fullstackcert.todo.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
        private val LOGIN_TIME_KEY = longPreferencesKey("login_time")
        private val REMEMBER_ME_KEY = androidx.datastore.preferences.core.booleanPreferencesKey("remember_me")
        private val SAVED_USERNAME_KEY = stringPreferencesKey("saved_username")
        private const val SESSION_DURATION_MS = 24 * 60 * 60 * 1000L
        private const val REMEMBER_ME_DURATION_MS = 30L * 24 * 60 * 60 * 1000L
    }

    val token: Flow<String?> = context.dataStore.data.map { it[TOKEN_KEY] }

    suspend fun getSavedCredentials(): Pair<String, Boolean> {
        val prefs = context.dataStore.data.first()
        val rememberMe = prefs[REMEMBER_ME_KEY] ?: false
        val username = if (rememberMe) prefs[SAVED_USERNAME_KEY] ?: "" else ""
        return Pair(username, rememberMe)
    }

    suspend fun saveSession(token: String, rememberMe: Boolean = false, username: String = "") {
        context.dataStore.edit {
            it[TOKEN_KEY] = token
            it[LOGIN_TIME_KEY] = System.currentTimeMillis()
            it[REMEMBER_ME_KEY] = rememberMe
            if (rememberMe) it[SAVED_USERNAME_KEY] = username else it.remove(SAVED_USERNAME_KEY)
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun isSessionValid(): Boolean {
        val prefs = context.dataStore.data.first()
        val token = prefs[TOKEN_KEY] ?: return false
        val loginTime = prefs[LOGIN_TIME_KEY] ?: return false
        val rememberMe = prefs[REMEMBER_ME_KEY] ?: false
        val duration = if (rememberMe) REMEMBER_ME_DURATION_MS else SESSION_DURATION_MS
        return token.isNotEmpty() && (System.currentTimeMillis() - loginTime) < duration
    }
}
