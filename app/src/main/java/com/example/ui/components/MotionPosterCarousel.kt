package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraGold
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * 3D Motion Poster Carousel with staggered entrance animation.
 *
 * When the app first launches, each carousel item fades in and slides up
 * with a staggered delay across items, settling smoothly into a 3D Cover Flow layout.
 */
@Composable
fun MotionPosterCarousel(
    items: List<MediaItem>,
    onSelectMedia: (MediaItem) -> Unit,
    modifier: Modifier = Modifier,
    onPlayTick: () -> Unit = {}
) {
    if (items.isEmpty()) return

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Default to the middle poster active (like cover-flow)
    val initialActiveIndex = (items.size / 2).coerceIn(0, items.size - 1)
    var currentIndex by remember { mutableIntStateOf(initialActiveIndex) }
    val animatedIndex = remember { Animatable(initialActiveIndex.toFloat()) }

    // Staggered Entrance Animation State for each item
    // Each item starts invisible (alpha = 0) and translated downwards (+75dp),
    // then cascades in sequentially when the app launches.
    val entranceAnimatables = remember(items.size) {
        List(items.size) { Animatable(0f) }
    }

    LaunchedEffect(items) {
        entranceAnimatables.forEachIndexed { index, animatable ->
            launch {
                // Staggered delay for cascading entrance: 85ms per card
                delay(index * 85L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = 0.74f, // Gentle natural spring
                        stiffness = 260f      // Polished response
                    )
                )
            }
        }
    }

    // Keep animatedIndex in sync when currentIndex updates via buttons
    fun navigateTo(index: Int) {
        val target = index.coerceIn(0, items.size - 1)
        if (target != currentIndex) {
            onPlayTick()
            currentIndex = target
            coroutineScope.launch {
                animatedIndex.animateTo(
                    targetValue = target.toFloat(),
                    animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("motion_poster_carousel"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8))
                )
                Text(
                    text = "MOTION POSTERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VeyraTextMuted,
                    letterSpacing = 2.4.sp
                )
            }

            Text(
                text = "${currentIndex + 1} / ${items.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = VeyraTextMuted
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3D Carousel Viewport
        val posterWidth = 190.dp
        val posterHeight = 275.dp
        val cardSpacingPx = with(density) { 135.dp.toPx() }

        var dragAccumulator by remember { mutableFloatStateOf(0f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(310.dp)
                .pointerInput(items.size) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            dragAccumulator = animatedIndex.value
                        },
                        onDragEnd = {
                            val nearest = animatedIndex.value.roundToInt().coerceIn(0, items.size - 1)
                            navigateTo(nearest)
                        },
                        onDragCancel = {
                            val nearest = animatedIndex.value.roundToInt().coerceIn(0, items.size - 1)
                            navigateTo(nearest)
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            // 1 index unit = 140dp of drag
                            val dragDeltaIndex = -dragAmount / with(density) { 140.dp.toPx() }
                            val newTarget = (animatedIndex.value + dragDeltaIndex).coerceIn(-0.25f, (items.size - 1) + 0.25f)
                            coroutineScope.launch {
                                animatedIndex.snapTo(newTarget)
                                currentIndex = animatedIndex.value.roundToInt().coerceIn(0, items.size - 1)
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Render items in layered 3D space
            items.forEachIndexed { index, mediaItem ->
                val offset = index - animatedIndex.value
                val absOffset = abs(offset)
                val direction = if (offset > 0) 1f else if (offset < 0) -1f else 0f

                // Entrance animation progress for this specific item
                val entranceProgress = entranceAnimatables.getOrNull(index)?.value ?: 1f
                val entranceAlpha = entranceProgress.coerceIn(0f, 1f)
                val entranceSlideY = with(density) { (75.dp * (1f - entranceProgress)).toPx() }

                // 3D Cover Flow Transformations
                val isActive = absOffset < 0.45f
                val cardScale = (1f - absOffset * 0.15f).coerceIn(0.68f, 1f)
                val cardAlpha = (1f - absOffset * 0.26f).coerceIn(0.25f, 1f) * entranceAlpha
                val cardRotationY = if (absOffset < 0.05f) 0f else (direction * -36f * (absOffset.coerceAtMost(1.4f) / 1.4f))
                val translationX = offset * cardSpacingPx

                Box(
                    modifier = Modifier
                        .zIndex(20f - absOffset) // Active and closer cards render on top
                        .graphicsLayer {
                            // 3D Perspective Depth (matching 1000px perspective)
                            this.cameraDistance = 32f
                            this.rotationY = cardRotationY
                            this.translationX = translationX
                            this.translationY = entranceSlideY
                            this.scaleX = cardScale
                            this.scaleY = cardScale
                            this.alpha = cardAlpha
                            this.shadowElevation = if (isActive) with(density) { 26.dp.toPx() } else with(density) { 6.dp.toPx() }
                        }
                        .width(posterWidth)
                        .height(posterHeight)
                        .shadow(
                            elevation = if (isActive) 24.dp else 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = if (isActive) Color(0x900284C7) else Color.Black,
                            spotColor = if (isActive) Color(0x6038BDF8) else Color.Black
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(VeyraCard)
                        .border(
                            width = if (isActive) 1.5.dp else 1.dp,
                            brush = if (isActive) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF38BDF8),
                                        Color(0x400284C7),
                                        Color(0x1538BDF8)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        VeyraBorder,
                                        Color.Transparent
                                    )
                                )
                            },
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            if (isActive) {
                                onSelectMedia(mediaItem)
                            } else {
                                navigateTo(index)
                            }
                        }
                        .testTag("motion_poster_item_$index")
                ) {
                    // Movie Poster Image
                    AsyncImage(
                        model = mediaItem.poster,
                        contentDescription = mediaItem.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scrim overlay for non-active cards (brightness falloff matching CSS filter: brightness(0.4))
                    if (!isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = (absOffset * 0.38f).coerceIn(0.15f, 0.65f)))
                        )
                    }

                    // Glass Badge at bottom of active card
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xCC000000),
                                            Color(0xF00A0A0C)
                                        )
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = mediaItem.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeyraWhite,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = VeyraGold,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Text(
                                            text = String.format("%.1f", mediaItem.rating),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VeyraGold
                                        )
                                    }

                                    Text(
                                        text = "•",
                                        fontSize = 10.sp,
                                        color = VeyraTextMuted
                                    )

                                    Text(
                                        text = mediaItem.year.toString(),
                                        fontSize = 10.sp,
                                        color = VeyraTextSecondary
                                    )

                                    if (mediaItem.genres.isNotEmpty()) {
                                        Text(
                                            text = "•",
                                            fontSize = 10.sp,
                                            color = VeyraTextMuted
                                        )
                                        Text(
                                            text = mediaItem.genres.first(),
                                            fontSize = 10.sp,
                                            color = VeyraTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation Controls: Prev, Indicator Dots, Next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prev Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (currentIndex > 0) VeyraCardElevated else VeyraCard.copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        if (currentIndex > 0) VeyraBorder else VeyraBorder.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(enabled = currentIndex > 0) {
                        navigateTo(currentIndex - 1)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("motion_carousel_prev_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Poster",
                        tint = if (currentIndex > 0) VeyraWhite else VeyraTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "PREV",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = if (currentIndex > 0) VeyraWhite else VeyraTextMuted.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.indices.forEach { index ->
                    val isDotActive = index == currentIndex
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(if (isDotActive) 18.dp else 5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDotActive) Color(0xFF38BDF8) else VeyraBorder
                            )
                            .clickable { navigateTo(index) }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Next Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (currentIndex < items.size - 1) VeyraCardElevated else VeyraCard.copy(alpha = 0.4f))
                    .border(
                        1.dp,
                        if (currentIndex < items.size - 1) VeyraBorder else VeyraBorder.copy(alpha = 0.3f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(enabled = currentIndex < items.size - 1) {
                        navigateTo(currentIndex + 1)
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("motion_carousel_next_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = if (currentIndex < items.size - 1) VeyraWhite else VeyraTextMuted.copy(alpha = 0.5f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Poster",
                        tint = if (currentIndex < items.size - 1) VeyraWhite else VeyraTextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Active Item Quick Action / Explore Prompt
        val activeMedia = items.getOrNull(currentIndex)
        if (activeMedia != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x1838BDF8))
                    .border(1.dp, Color(0x3538BDF8), RoundedCornerShape(12.dp))
                    .clickable { onSelectMedia(activeMedia) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Tap to view ${activeMedia.title}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF38BDF8)
                )
            }
        }
    }
}
