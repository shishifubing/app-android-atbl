package com.shishifubing.atbl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.shishifubing.atbl.ui.UI
import kotlinx.coroutines.launch

private val tag = MainActivity::class.simpleName


class MainActivity : ComponentActivity() {

    private lateinit var app: LauncherApplication
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        app = (application as LauncherApplication).init(this)

        lifecycleScope.launch {
            app.stateRepo.reloadState()
        }
        app.manager.addCallback(
            onChanged = {
                lifecycleScope.launch {
                    app.stateRepo.reloadApp(it)
                }
            },
            onRemoved = {
                lifecycleScope.launch {
                    app.stateRepo.removeApp(it)
                }
            }
        )

        setContent {
            UI()
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            app.stateRepo.updateIsHomeApp()
        }
    }

    override fun onStop() {
        super.onStop()
        app.manager.removeCallbacks()
    }
}


