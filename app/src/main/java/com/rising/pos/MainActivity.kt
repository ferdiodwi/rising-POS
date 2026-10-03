package com.rising.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.rising.pos.core.datastore.AppPreferences
import com.rising.pos.core.datastore.BusinessSettings
import com.rising.pos.core.model.AppTheme
import com.rising.pos.core.update.AppUpdateManager
import com.rising.pos.core.update.model.UpdateState
import com.rising.pos.feature.settings.components.AppUpdateDialog
import com.rising.pos.ui.navigation.PosAppNavHost
import com.rising.pos.ui.theme.RisingPosTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var appUpdateManager: AppUpdateManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by appPreferences.settingsFlow.collectAsState(initial = null)
            val updateState by appUpdateManager.updateState.collectAsState()
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(Unit) {
                // Auto-check for updates at startup (triggers force update if below min_version_code)
                appUpdateManager.checkForUpdates()
            }

            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = when (settings?.appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                else -> systemDark
            }

            androidx.compose.runtime.DisposableEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = androidx.activity.SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { isDarkTheme },
                    navigationBarStyle = androidx.activity.SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    ) { isDarkTheme }
                )
                onDispose {}
            }

            RisingPosTheme(darkTheme = isDarkTheme) {
                PosAppNavHost(
                    appPreferences = appPreferences,
                    settings = settings
                )

                // ── Global Force Update Modal ────────────────────────────────
                val isForceUpdateActive = when (val state = updateState) {
                    is UpdateState.UpdateAvailable -> state.isForceUpdate
                    is UpdateState.Downloading -> state.isForceUpdate
                    is UpdateState.Downloaded -> state.isForceUpdate
                    is UpdateState.Error -> state.isForceUpdate
                    else -> false
                }

                if (isForceUpdateActive) {
                    AppUpdateDialog(
                        updateState = updateState,
                        canInstallPackages = appUpdateManager.canInstallPackages(),
                        onDownload = { url, tag, isForce ->
                            coroutineScope.launch {
                                appUpdateManager.downloadApk(url, tag, isForce)
                            }
                        },
                        onInstall = { appUpdateManager.installApk() },
                        onOpenUnknownSourcesSettings = {
                            appUpdateManager.getUnknownSourcesSettingsIntent()?.let { intent ->
                                startActivity(intent)
                            }
                        },
                        onDismiss = { /* Non-dismissible when force update is active */ }
                    )
                }
            }
        }
    }
}
