package com.rising.pos.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/** Full page on phones, constrained panel on tablets. Callers keep actions outside scroll content. */
@Composable
fun PosDialog(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp
    val isPhonePortrait = screenWidth < 600
    val isCompactHeight = screenHeight < 500

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(
            modifier = when {
                isPhonePortrait -> Modifier.fillMaxSize()
                isCompactHeight -> Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.96f)
                else -> Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            },
            shape = if (isPhonePortrait) RectangleShape else RoundedCornerShape(if (isCompactHeight) 14.dp else 20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .imePadding()
                    .padding(
                        horizontal = if (isCompactHeight) 16.dp else 20.dp,
                        vertical = if (isCompactHeight) 10.dp else 20.dp
                    ),
                content = content
            )
        }
    }
}
