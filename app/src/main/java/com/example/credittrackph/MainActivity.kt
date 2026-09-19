package com.example.credittrackph

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.credittrackph.notification.BudgetAlertWorker
import com.example.credittrackph.notification.DueDateReminderWorker
import com.example.credittrackph.security.BiometricAuthManager
import com.example.credittrackph.theme.*
import com.example.credittrackph.util.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    @Inject lateinit var preferencesManager: PreferencesManager
    @Inject lateinit var biometricAuthManager: BiometricAuthManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic background workers
        DueDateReminderWorker.schedule(this)
        BudgetAlertWorker.schedule(this)

        val permissionsToRequest = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())

        setContent {
            val appTheme by preferencesManager.appTheme.collectAsState(initial = "DARK")
            val isDark = appTheme != "LIGHT"
            val isBiometricEnabled by preferencesManager.biometricEnabled.collectAsState(initial = false)
            val scope = rememberCoroutineScope()

            var isUnlocked by remember { mutableStateOf(false) }

            // Auto-prompt biometric if enabled
            LaunchedEffect(isBiometricEnabled) {
                if (isBiometricEnabled && !isUnlocked) {
                    biometricAuthManager.authenticate(
                        activity = this@MainActivity,
                        onSuccess = { isUnlocked = true },
                        onError = { /* Wait for manual click */ }
                    )
                }
            }

            CreditTrackPHTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (isBiometricEnabled && !isUnlocked) {
                        // Biometric Lock Screen
                        LockScreen(
                            onUnlockClick = {
                                biometricAuthManager.authenticate(
                                    activity = this@MainActivity,
                                    onSuccess = { isUnlocked = true },
                                    onError = { /* Error feedback handled by prompt */ }
                                )
                            }
                        )
                    } else {
                        MainNavigation(
                            isDarkTheme = isDark,
                            onToggleTheme = {
                                scope.launch {
                                    preferencesManager.setAppTheme(if (isDark) "LIGHT" else "DARK")
                                }
                            },
                            isBiometricEnabled = isBiometricEnabled,
                            onToggleBiometric = { enabled ->
                                scope.launch {
                                    preferencesManager.setBiometricEnabled(enabled)
                                    if (!enabled) isUnlocked = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LockScreen(onUnlockClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(appBackgroundColor())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(appSoftSuccessColor(), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = appPrimaryColor(),
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "CreditTrack PH",
                color = appTextColor(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Biometric lock active.\nUnlock to view your credit cards & dues.",
                color = appTextSubColor(),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onUnlockClick,
                colors = ButtonDefaults.buttonColors(containerColor = appAccentColor()),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = appOnAccentColor())
                Spacer(Modifier.width(8.dp))
                Text(
                    "Unlock with Biometrics",
                    color = appOnAccentColor(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
