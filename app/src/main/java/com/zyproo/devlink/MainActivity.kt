package com.zyproo.devlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.zyproo.devlink.ui.navigation.DevLinkNavHost
import com.zyproo.devlink.ui.theme.DevLinkTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevLinkTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DevLinkNavHost()
                }
            }
        }
    }
}
