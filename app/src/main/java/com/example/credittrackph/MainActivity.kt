package com.example.credittrackph

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android.Manifest
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.example.credittrackph.notification.DueDateReminderWorker
import com.example.credittrackph.theme.CreditTrackPHTheme
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.credittrackph.util.PreferencesManager
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var preferencesManager: PreferencesManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Schedule daily due date reminder
        DueDateReminderWorker.schedule(this)

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
            val scope = rememberCoroutineScope()

            CreditTrackPHTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainNavigation(
                        isDarkTheme = isDark,
                        onToggleTheme = {
                            scope.launch {
                                preferencesManager.setAppTheme(if (isDark) "LIGHT" else "DARK")
                            }
                        }
                    )
                }
            }
        }
    }
}
