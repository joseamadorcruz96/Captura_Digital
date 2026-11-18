package com.therionlabs.captura_digital_v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.therionlabs.captura_digital_v2.ui.auth.AuthNavigation
import com.therionlabs.captura_digital_v2.ui.theme.Captura_Digital_v2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Captura_Digital_v2Theme {
                AuthNavigation(onLoginSuccess = {})
            }
        }
    }
}