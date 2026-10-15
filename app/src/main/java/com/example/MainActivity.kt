package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.example.automation.AutomationWorker
import com.example.ui.NavioApp
import com.example.ui.theme.NavioTheme
import com.example.viewmodel.AppMode
import com.example.viewmodel.NavioViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NavioViewModel by viewModels()

    private val notificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AutomationWorker.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        handleIntent(intent)
        setContent {
            NavioTheme {
                NavioApp(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        if (!handledAuthCallback) viewModel.onResumeWithoutCallback()
        handledAuthCallback = false
    }

    private var handledAuthCallback = false

    private fun handleIntent(intent: Intent?) {
        if (viewModel.handleAuthCallback(intent?.data)) {
            handledAuthCallback = true
            return
        }
        if (intent?.getBooleanExtra(EXTRA_OPEN_COMPUTER, false) == true) viewModel.setMode(AppMode.COMPUTER)
    }

    companion object {
        const val EXTRA_OPEN_COMPUTER = "open_computer"
    }
}
