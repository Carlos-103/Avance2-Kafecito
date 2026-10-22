package com.kafecito.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.kafecito.app.navigation.KafecitoNavGraph
import com.kafecito.app.ui.theme.KafecitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KafecitoTheme {
                KafecitoNavGraph()
            }
        }
    }
}
