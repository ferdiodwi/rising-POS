package com.rising.pos.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.indication
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rising.pos.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch

// ── Google Play Store Color Palette ──────────────────────────────────────────
// Dark Theme Colors
private val DarkNavBg = Color(0xFF131316)              // Deep charcoal surface
private val DarkNavBorder = Color(0xFF27272A)          // Subtle top divider
private val DarkActivePill = Color(0xFF004A77)         // Deep Google Blue stadium pill
private val DarkActiveIcon = Color(0xFFC2E7FF)         // Light Sky Blue active icon
private val DarkActiveText = Color(0xFF7FCFFF)         // Vibrant light cyan-blue active label
private val DarkInactiveIcon = Color(0xFF94A3B8)       // Crisp muted slate inactive icon
private val DarkInactiveText = Color(0xFF94A3B8)       // Inactive label
private val DarkRippleColor = Color(0xFF7FCFFF).copy(alpha = 0.25f) // Active Blue Ripple (NO GREY)

// Light Theme Colors (Official Google Play Store / Material 3)
private val LightNavBg = Color.White
private val LightNavBorder = Color(0xFFE2E8F0)
private val LightActivePill = Color(0xFFD3E3FD)        // Authentic Google M3 Soft Sky Blue pill
private val LightActiveIcon = Color(0xFF041E49)        // Deep Google Navy active icon (high contrast & crystal clear)
private val LightActiveText = Color(0xFF041E49)        // Deep Google Navy active label
private val LightInactiveIcon = Color(0xFF475569)      // Balanced slate inactive icon
private val LightInactiveText = Color(0xFF475569)      // Balanced slate inactive label
private val LightRippleColor = Color(0xFF0B57D0).copy(alpha = 0.16f) // Smooth Blue Ripple

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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
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

@Composable
private fun PlayStoreNavItem(
    screen: Screen,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activePillColor = if (isDarkTheme) DarkActivePill else LightActivePill
    val activeIconColor = if (isDarkTheme) DarkActiveIcon else LightActiveIcon
    val activeTextColor = if (isDarkTheme) DarkActiveText else LightActiveText
    val inactiveIconColor = if (isDarkTheme) DarkInactiveIcon else LightInactiveIcon
    val inactiveTextColor = if (isDarkTheme) DarkInactiveText else LightInactiveText
    val clickRippleColor = if (isDarkTheme) DarkRippleColor else LightRippleColor

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

    // Animasi Pill Lebar & Fade (Smooth blooming pill pada tab yang aktif)
    val pillScale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.82f,
            stiffness = 500f
        ),
        label = "pillScale"
    )

    // ── Animasi Profesional: Spring Scale Pop (Tanpa 3D Flip) ────────────────
    val iconScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    fun triggerSpringPop() {
        coroutineScope.launch {
            iconScale.animateTo(
                targetValue = 1.18f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            iconScale.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    LaunchedEffect(isSelected) {
        if (isSelected) {
            triggerSpringPop()
        } else {
            iconScale.snapTo(1f)
        }
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null // Matikan ripple global agar tidak meluber bulat ke teks
            ) {
                if (isSelected) {
                    triggerSpringPop()
                }
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Kontainer Pill + Ikon (Bentuk kapsul 64.dp x 30.dp dengan radius 15.dp)
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .indication(
                        interactionSource = interactionSource,
                        indication = ripple(
                            bounded = true,
                            color = clickRippleColor
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Background Stadium Pill (muncul langsung pada tab yang aktif)
                if (pillScale > 0.01f) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = 64.dp * pillScale,
                                height = 30.dp
                            )
                            .background(
                                color = activePillColor.copy(alpha = pillScale),
                                shape = RoundedCornerShape(15.dp)
                            )
                    )
                }

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
                                scaleX = iconScale.value
                                scaleY = iconScale.value
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(1.dp))

            Text(
                text = screen.title,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = animatedTextColor,
                maxLines = 1
            )
        }
    }
}
