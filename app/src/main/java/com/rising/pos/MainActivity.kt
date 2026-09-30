package com.rising.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.AppTheme
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
            val settings by appPreferences.settingsFlow.collectAsState(initial = BusinessSettings())
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings.appTheme) {
                AppTheme.SYSTEM -> systemDark
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            RisingPosTheme(darkTheme = isDarkTheme) {
                PosAppNavHost(appPreferences = appPreferences)
            }
        }
    }
}
