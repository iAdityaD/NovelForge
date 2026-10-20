package com.novelforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.novelforge.app.ui.*

@dagger.hilt.android.AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val editor: com.novelforge.presentation.EditorViewModel by viewModels()
    private val settings: com.novelforge.presentation.SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val prefs by settings.state.collectAsStateWithLifecycle()
            ForgeTheme(prefs.dark) {
                androidx.compose.runtime.SideEffect {
                    androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                        .apply {
                            isAppearanceLightStatusBars = !prefs.dark
                            isAppearanceLightNavigationBars = !prefs.dark
                        }
                }
                ForgeApp()
            }
        }
    }

    override fun onStop() {
        editor.flushBackground()
        super.onStop()
    }
}
