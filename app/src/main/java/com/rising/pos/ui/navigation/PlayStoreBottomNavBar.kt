package com.rising.pos.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.ui.theme.PrimaryBlue
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// Google Play Store Palette (matching user screenshot)
// Dark Theme Colors
private val DarkNavBg = Color(0xFF131316)              // Deep charcoal surface
private val DarkNavBorder = Color(0xFF27272A)          // Subtle top divider
private val DarkActivePill = Color(0xFF004A77)         // Deep Google Blue stadium pill
private val DarkActiveIcon = Color(0xFFC2E7FF)         // Light Sky Blue active icon
private val DarkActiveText = Color(0xFF7FCFFF)         // Vibrant light cyan-blue active label
private val DarkInactiveIcon = Color(0xFFC4C7C5)       // Crisp light slate inactive icon
private val DarkInactiveText = Color(0xFFC4C7C5)       // Inactive label

// Light Theme Colors
private val LightNavBg = Color.White
private val LightNavBorder = Color(0xFFE2E8F0)
private val LightActivePill = Color(0xFFD3E3FD)        // Soft M3 blue pill
private val LightActiveIcon = Color(0xFF001D35)        // Navy active icon
private val LightActiveText = PrimaryBlue              // Blue active label
private val LightInactiveIcon = Color(0xFF64748B)       // Slate inactive icon
private val LightInactiveText = Color(0xFF64748B)       // Slate inactive label

@Composable
fun PlayStoreBottomNavBar(
    items: List<Screen>,
    currentRoute: String,
    isDarkTheme: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBg = if (isDarkTheme) DarkNavBg else LightNavBg
    val navBorder = if (isDarkTheme) DarkNavBorder else LightNavBorder
    val activePillColor = if (isDarkTheme) DarkActivePill else LightActivePill

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)

    Surface(
        color = navBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                thickness = 0.8.dp,
                color = navBorder
            )

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                val totalWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
                val tabCount = items.size.coerceAtLeast(1)
                val tabWidthPx = totalWidthPx / tabCount

                val targetPillCx = tabWidthPx * (selectedIndex + 0.5f)
                val animatedPillCx by animateFloatAsState(
                    targetValue = targetPillCx,
                    animationSpec = spring(
                        dampingRatio = 0.88f, // High damping: smooth, organic glide without overshoot wobble
                        stiffness = 400f      // Responsive, fluid transition
                    ),
                    label = "pillCenterX"
                )

                val pillWidth = 64.dp
                val pillHeight = 32.dp
                val pillWidthPx = with(LocalDensity.current) { pillWidth.toPx() }
                val pillTopPx = with(LocalDensity.current) { 8.dp.toPx() }

                // ── Single Unified Sliding Stadium Pill ─────────────────────
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = (animatedPillCx - pillWidthPx / 2f).roundToInt(),
                                y = pillTopPx.roundToInt()
                            )
                        }
                        .size(width = pillWidth, height = pillHeight)
                        .background(
                            color = activePillColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                )

                // ── Tab Items Row ───────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEachIndexed { index, screen ->
                        val isSelected = index == selectedIndex
                        PlayStoreNavItem(
                            screen = screen,
                            isSelected = isSelected,
                            isDarkTheme = isDarkTheme,
                            onClick = { onNavigate(screen.route) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayStoreNavItem(
    screen: Screen,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeIconColor = if (isDarkTheme) DarkActiveIcon else LightActiveIcon
    val activeTextColor = if (isDarkTheme) DarkActiveText else LightActiveText
    val inactiveIconColor = if (isDarkTheme) DarkInactiveIcon else LightInactiveIcon
    val inactiveTextColor = if (isDarkTheme) DarkInactiveText else LightInactiveText

    val animatedIconColor by animateColorAsState(
        targetValue = if (isSelected) activeIconColor else inactiveIconColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "iconColor"
    )
    val animatedTextColor by animateColorAsState(
        targetValue = if (isSelected) activeTextColor else inactiveTextColor,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "textColor"
    )

    // ── Butter-Smooth 3D Flip & Scale Animation ─────────────────────────────
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()
    var flipJob by remember { mutableStateOf<Job?>(null) }

    fun triggerFlip() {
        flipJob?.cancel()
        flipJob = coroutineScope.launch {
            rotationAnim.snapTo(0f)
            scaleAnim.snapTo(1f)

            // Smooth 3D Y-axis flip with Material FastOutSlowInEasing (no jerky wobble or overshoot)
            launch {
                rotationAnim.animateTo(
                    targetValue = 360f,
                    animationSpec = tween(
                        durationMillis = 380,
                        easing = FastOutSlowInEasing
                    )
                )
                rotationAnim.snapTo(0f)
            }

            // Synchronized gentle scale pop (bulges to 1.14x mid-spin, smoothly returns to 1.0x)
            launch {
                scaleAnim.animateTo(
                    targetValue = 1.14f,
                    animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
                )
                scaleAnim.animateTo(
                    targetValue = 1.0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            triggerFlip()
        } else {
            flipJob?.cancel()
            rotationAnim.snapTo(0f)
            scaleAnim.snapTo(1f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 34.dp)
            ) {
                if (isSelected) {
                    // Already selected: user taps again to see the flip animation
                    triggerFlip()
                }
                // When selecting a new tab, LaunchedEffect(isSelected) will trigger the single flip cleanly
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(width = 64.dp, height = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                val icon = if (isSelected) {
                    screen.selectedIcon ?: screen.icon
                } else {
                    screen.unselectedIcon ?: screen.icon
                }

                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = screen.title,
                        tint = animatedIconColor,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationY = rotationAnim.value
                                scaleX = scaleAnim.value
                                scaleY = scaleAnim.value
                                cameraDistance = 24f * density
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = screen.title,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = animatedTextColor,
                maxLines = 1
            )
        }
    }
}

