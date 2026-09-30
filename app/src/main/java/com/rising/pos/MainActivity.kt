package com.rising.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.ui.navigation.PosAppNavHost
import com.rising.pos.ui.theme.RisingPosTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RisingPosTheme {
                PosAppNavHost(appPreferences = appPreferences)
            }
        }
    }
}
