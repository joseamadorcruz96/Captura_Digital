package com.therionlabs.captura_digital_v2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.therionlabs.captura_digital_v2.ui.auth.AuthNavigation
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

class MainActivity : ComponentActivity() {

    private val localNetworkPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 37 &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
        enableEdgeToEdge()
        setContent {
            Captura_Digital_v2Theme {
                AuthNavigation(onLoginSuccess = {})
            }
        }
    }
}