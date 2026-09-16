package com.example.credittrackph.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "credittrack_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val GROQ_API_KEY = stringPreferencesKey("groq_api_key")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val APP_THEME = stringPreferencesKey("app_theme") // DARK, LIGHT, AMOLED
        val SMS_PARSING_ENABLED = booleanPreferencesKey("sms_parsing_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { it[BIOMETRIC_ENABLED] ?: false }
    val appTheme: Flow<String> = context.dataStore.data.map { it[APP_THEME] ?: "DARK" }
    val smsParsingEnabled: Flow<Boolean> = context.dataStore.data.map { it[SMS_PARSING_ENABLED] ?: true }

    suspend fun getGroqApiKey(): String? = context.dataStore.data.first()[GROQ_API_KEY]
    suspend fun setGroqApiKey(key: String) { context.dataStore.edit { it[GROQ_API_KEY] = key } }
    suspend fun setBiometricEnabled(enabled: Boolean) { context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled } }
    suspend fun setAppTheme(theme: String) { context.dataStore.edit { it[APP_THEME] = theme } }
    suspend fun setSmsParsingEnabled(enabled: Boolean) { context.dataStore.edit { it[SMS_PARSING_ENABLED] = enabled } }
    suspend fun setOnboardingDone() { context.dataStore.edit { it[ONBOARDING_DONE] = true } }
    suspend fun isOnboardingDone(): Boolean = context.dataStore.data.first()[ONBOARDING_DONE] ?: false
}
