package com.zyproo.devlink.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.zyproo.devlink.data.remote.dto.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_ID = stringPreferencesKey("user_id")
        val USERNAME = stringPreferencesKey("username")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val SERVER_HOST = stringPreferencesKey("server_host")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val accessTokenFlow: Flow<String?> = dataStore.data.map { it[Keys.ACCESS_TOKEN] }
    val roleFlow: Flow<String?> = dataStore.data.map { it[Keys.ROLE] }
    val serverHostFlow: Flow<String?> = dataStore.data.map { it[Keys.SERVER_HOST] }
    val isLoggedInFlow: Flow<Boolean> = dataStore.data.map { !it[Keys.ACCESS_TOKEN].isNullOrBlank() }
    val userIdFlow: Flow<Long?> = dataStore.data.map { it[Keys.USER_ID]?.toLongOrNull() }
    val usernameFlow: Flow<String?> = dataStore.data.map { it[Keys.USERNAME] }
    val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_COMPLETED] == true }

    suspend fun setOnboardingCompleted(completed: Boolean = true) {
        dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun saveSession(auth: AuthResponse) {
        dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = auth.accessToken
            prefs[Keys.REFRESH_TOKEN] = auth.refreshToken
            prefs[Keys.USER_ID] = auth.userId.toString()
            prefs[Keys.USERNAME] = auth.username
            prefs[Keys.EMAIL] = auth.email
            prefs[Keys.ROLE] = auth.role
        }
    }

    suspend fun saveServerHost(host: String) {
        dataStore.edit { prefs ->
            prefs[Keys.SERVER_HOST] = host.trim()
        }
    }

    suspend fun clearSession() {
        dataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USERNAME)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.ROLE)
        }
    }
}
