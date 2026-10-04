package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeChild
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

enum class BottomNavDestination(
    val screen: AppScreen,
    val label: String,
    val icon: ImageVector,
    val testTag: String
) {
    DISCOVER(AppScreen.WELCOME, "Discover", Icons.Default.Explore, "nav_discover_tab"),
    SEARCH(AppScreen.SEARCH, "Search", Icons.Default.Search, "nav_search_tab"),
    WATCHLIST(AppScreen.WATCHLIST, "Watchlist", Icons.Default.Bookmark, "nav_watchlist_tab"),
    HISTORY(AppScreen.HISTORY, "History", Icons.Default.History, "nav_history_tab")
}

@Composable
fun FloatingBottomBar(
    hazeState: HazeState? = null,
    currentScreen: AppScreen,
    watchlistCount: Int,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val destinations = BottomNavDestination.values()
    val targetIndex = destinations.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)

    val coroutineScope = rememberCoroutineScope()
    val pillProgress = remember { Animatable(targetIndex.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }

    // High-performance frosted glass style (zero noise calculation overhead)
    val frostedStyle = remember {
        HazeStyle(
            backgroundColor = Color(0xFF0F1015),
            tint = HazeTint(Color(0x751A1A24)),
            blurRadius = 14.dp,
            noiseFactor = 0f
        )
    }

    // SINGLE SOURCE OF TRUTH:
    // When currentScreen updates, animate the white pill to the matching tab!
    LaunchedEffect(currentScreen) {
        val newTarget = destinations.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)
        pillProgress.animateTo(
            targetValue = newTarget.toFloat(),
            animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = Color.Black.copy(alpha = 0.4f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .clip(RoundedCornerShape(percent = 50))
                .then(
                    if (hazeState != null) {
                        Modifier.hazeChild(state = hazeState, style = frostedStyle)
                    } else Modifier
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x751A1A24),
                            Color(0x55101016)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x55FFFFFF),
                            Color(0x18FFFFFF)
                        )
                    ),
                    shape = RoundedCornerShape(percent = 50)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .pointerInput(currentScreen) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                val totalW = size.width.toFloat()
                                val tabW = totalW / 4f
                                val dragTarget = ((offset.x - (tabW / 2f)) / tabW).coerceIn(0f, 3f)
                                coroutineScope.launch {
                                    pillProgress.snapTo(dragTarget)
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val totalW = size.width.toFloat()
                                val tabW = totalW / 4f
                                val newProgress = (pillProgress.value + (dragAmount.x / tabW)).coerceIn(0f, 3f)
                                coroutineScope.launch {
                                    pillProgress.snapTo(newProgress)
                                }
                            },
                            onDragEnd = {
                                isDragging = false
                                val finalIndex = pillProgress.value.roundToInt().coerceIn(0, 3)
                                val dest = destinations[finalIndex]
                                onNavigate(dest.screen)
                            },
                            onDragCancel = {
                                isDragging = false
                                val returnIndex = destinations.indexOfFirst { it.screen == currentScreen }.coerceAtLeast(0)
                                coroutineScope.launch {
                                    pillProgress.animateTo(
                                        targetValue = returnIndex.toFloat(),
                                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f)
                                    )
                                }
                            }
                        )
                    }
            ) {
                val totalWidth = maxWidth
                val tabWidth = totalWidth / 4f
                val progress = pillProgress.value

                // Standard pill width fitting comfortably inside the tab slot
                val basePillWidth = tabWidth - 8.dp
                // Subtle stretch during transit for fluid liquid-motion effect
                val transitFraction = abs(progress - progress.roundToInt().toFloat())
                val stretch = (sin(transitFraction * Math.PI.toFloat()) * 8f).dp
                val currentPillWidth = basePillWidth + stretch

                val pillLeft = (tabWidth * progress) + ((tabWidth - currentPillWidth) / 2f)

                // 1. Sliding Fluid Active Pill (White Capsule)
                Box(
                    modifier = Modifier
                        .offset { IntOffset(pillLeft.roundToPx(), 0) }
                        .width(currentPillWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color.White)
                )

                // 2. Navigation Items Row
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    destinations.forEachIndexed { index, destination ->
                        val distance = abs(progress - index.toFloat())
                        val activeFraction = (1f - distance).coerceIn(0f, 1f)

                        // Subtle zoom-in / zoom-out (0.92f to 1.06f)
                        val scale = 0.92f + (0.14f * activeFraction)
                        // Smooth opacity transition (0.68f to 1.0f)
                        val opacity = 0.68f + (0.32f * activeFraction)

                        // Smooth interpolation from inactive gray (0x8E8E93) to active black (0x000000)
                        val r = (0x8E + ((0x00 - 0x8E) * activeFraction)).toInt().coerceIn(0, 255)
                        val g = (0x8E + ((0x00 - 0x8E) * activeFraction)).toInt().coerceIn(0, 255)
                        val b = (0x93 + ((0x00 - 0x93) * activeFraction)).toInt().coerceIn(0, 255)
                        val contentColor = Color(r, g, b)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(percent = 50))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    // DIRECT, UNCONDITIONAL NAVIGATION CALL ON TAP:
                                    onNavigate(destination.screen)
                                }
                                .testTag(destination.testTag),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    alpha = opacity
                                }
                            ) {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                    tint = contentColor,
                                    modifier = Modifier.size(21.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = destination.label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (activeFraction > 0.5f) FontWeight.Bold else FontWeight.Medium,
                                    color = contentColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
