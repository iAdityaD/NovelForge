package com.novelforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.novelforge.app.ui.*

class MainActivity : ComponentActivity() {
    private val vm: ForgeViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ForgeTheme(vm.dark) { ForgeApp(vm) } }
    }
    override fun onStop() { vm.flushBackground(); super.onStop() }
}
