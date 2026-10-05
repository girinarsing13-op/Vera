package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Real clean movie posters without placeholder text
private val SURPRISE_POSTERS = listOf(
    "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", // Interstellar
    "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg", // Oppenheimer
    "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg", // Dune: Part Two
    "https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg", // Blade Runner 2049
    "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Jz20NpMygczOkv.jpg"  // Severance
)

private val FLOATING_SPACE_POSTERS = listOf(
    "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
    "https://image.tmdb.org/t/p/w500/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
    "https://image.tmdb.org/t/p/w500/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
    "https://image.tmdb.org/t/p/w500/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg",
    "https://image.tmdb.org/t/p/w500/9PFonBhy4cQy7Jz20NpMygczOkv.jpg",
    "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg"
)

private val WELCOME_FEATURE_CHIPS = listOf(
    "Genres",
    "Eras",
    "Streaming",
    "Discovery Modes",
    "Moods",
    "Region & Language"
)

private data class StarParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val baseAlpha: Float,
    val twinkleSpeed: Float
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingOverlay(
    step: Int,
    onNext: () -> Unit,
    onFinish: () -> Unit = onNext,
    onPlayTactile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentSurpriseIndex by remember { mutableIntStateOf(0) }
    val surpriseFlipRotation = remember { Animatable(0f) }
    val surpriseScale = remember { Animatable(1f) }

    // Pre-allocated star field
    val stars = remember {
        val rand = Random(128)
        List(45) {
            StarParticle(
                x = rand.nextFloat(),
                y = rand.nextFloat(),
                radius = 1f + rand.nextFloat() * 1.6f,
                baseAlpha = 0.25f + rand.nextFloat() * 0.55f,
                twinkleSpeed = 0.8f + rand.nextFloat() * 1.5f
            )
        }
    }

    // High refresh rate continuous animation engine
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_motion_engine")

    val timeSeconds by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_seconds"
    )

    val orbPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_pulse"
    )

    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(28000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    val streakPulse by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.70f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("welcomeOverlay")
    ) {
        // Multi-Layered Celestial Stars & Atmospheric Volumetric Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Orb 1 (top-left sapphire/blue)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x451E3A8A),
                        Color(0x181E3A8A),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.25f, h * 0.22f),
                    radius = (w * 0.55f) * orbPulse
                ),
                radius = (w * 0.55f) * orbPulse,
                center = Offset(w * 0.25f, h * 0.22f)
            )

            // Orb 2 (right-center azure/cyan)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x350284C7),
                        Color(0x120284C7),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.46f),
                    radius = (w * 0.50f) * (2f - orbPulse)
                ),
                radius = (w * 0.50f) * (2f - orbPulse),
                center = Offset(w * 0.82f, h * 0.46f)
            )

            // Orb 3 (bottom violet glow)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x286366F1),
                        Color(0x0C6366F1),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.38f, h * 0.84f),
                    radius = (w * 0.60f) * orbPulse
                ),
                radius = (w * 0.60f) * orbPulse,
                center = Offset(w * 0.38f, h * 0.84f)
            )

            // Screen 1 Subtle Anamorphic Flare
            if (step == 1) {
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x1038BDF8).copy(alpha = 0.15f * streakPulse),
                            Color(0x5538BDF8).copy(alpha = 0.50f * streakPulse),
                            Color(0x1038BDF8).copy(alpha = 0.15f * streakPulse),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(0f, h * 0.46f - 1.5f),
                    size = Size(w, 3f)
                )
            }

            // Twinkling Star Motes
            stars.forEach { star ->
                val twinkle = (0.6f + 0.4f * sin(timeSeconds * star.twinkleSpeed + star.x * 6f))
                drawCircle(
                    color = Color.White.copy(alpha = (star.baseAlpha * twinkle).coerceIn(0.1f, 1f)),
                    radius = star.radius,
                    center = Offset(star.x * w, star.y * h)
                )
            }
        }

        // Screen 2: Floating space with REAL movie posters with continuous weightless drift
        if (step == 2) {
            Box(modifier = Modifier.fillMaxSize()) {
                FLOATING_SPACE_POSTERS.forEachIndexed { i, url ->
                    val (xRel, yRel, baseRotZ) = when (i) {
                        0 -> Triple(0.04f, 0.08f, -7f)
                        1 -> Triple(0.68f, 0.12f, 6f)
                        2 -> Triple(0.06f, 0.64f, 5f)
                        3 -> Triple(0.72f, 0.60f, -6f)
                        4 -> Triple(0.38f, 0.02f, 3f)
                        else -> Triple(0.40f, 0.74f, -4f)
                    }

                    // Continuous zero-cost weightless floating drift via trigonometric wave
                    val floatY = sin(timeSeconds + i * 1.2f) * 8f
                    val floatRot = baseRotZ + cos(timeSeconds + i * 0.8f) * 1.5f

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                translationX = xRel * 520f - 80f
                                translationY = (yRel * 780f - 120f) + floatY
                                rotationZ = floatRot
                                alpha = 0.30f
                            }
                            .size(width = 96.dp, height = 142.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(20.dp, RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Dark Shade Gradient (<div class="shade"></div>)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.45f),
                                    Color.Black.copy(alpha = 0.78f),
                                    Color.Black.copy(alpha = 0.92f)
                                )
                            )
                        )
                )
            }
        }

        // Main Screen Content Box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(420, easing = FastOutSlowInEasing)) { height -> height / 3 } + fadeIn(tween(400)))
                        .togetherWith(slideOutVertically(animationSpec = tween(320, easing = FastOutSlowInEasing)) { height -> -height / 3 } + fadeOut(tween(260)))
                },
                label = "welcome_screens"
            ) { currentStep ->
                when (currentStep) {
                    // SCREEN 1: Big Vera logo & full name with 3D counter-rotating rings
                    1 -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            // 3D Concentric Pulsing Rings with 3D perspective tilt
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .graphicsLayer {
                                        rotationZ = ringRotation
                                        rotationX = 14f
                                    }
                                    .border(1.dp, Color(0x3038BDF8), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(310.dp)
                                    .graphicsLayer {
                                        rotationZ = -ringRotation * 0.7f
                                        rotationX = 14f
                                    }
                                    .border(1.dp, Color(0x1E38BDF8), CircleShape)
                            )
                            Box(
                                modifier = Modifier
                                    .size(380.dp)
                                    .graphicsLayer {
                                        rotationZ = ringRotation * 0.4f
                                        rotationX = 14f
                                    }
                                    .border(1.dp, Color(0x1238BDF8), CircleShape)
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(22.dp)
                            ) {
                                // Logo: V E R A with individual subtle floating oscillations
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf("V", "e", "r", "a").forEachIndexed { idx, letter ->
                                        val letterFloat = sin(timeSeconds + idx * 0.9f) * 3f
                                        Text(
                                            text = letter,
                                            fontSize = 54.sp,
                                            fontWeight = FontWeight.Black,
                                            color = VeyraWhite,
                                            letterSpacing = 2.sp,
                                            modifier = Modifier.graphicsLayer {
                                                translationY = letterFloat
                                            }
                                        )
                                    }
                                }

                                // Acronym Frosted Glass Pill
                                Box(
                                    modifier = Modifier
                                        .graphicsLayer {
                                            translationY = sin(timeSeconds + 2f) * 2.5f
                                        }
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x381E293B),
                                                    Color(0x1C0F172A)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x5538BDF8),
                                                    Color(0x1838BDF8)
                                                )
                                            ),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .padding(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "Virtual Entertainment Recommendation Assistant",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE2E8F0),
                                        letterSpacing = 0.4.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // SCREEN 2: Feature Card: "Your taste, your rules."
                    2 -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(26.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xE00C0D14),
                                            Color(0xF506070A)
                                        )
                                    )
                                )
                                .border(
                                    1.dp,
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0x5538BDF8),
                                            Color(0x18FFFFFF)
                                        )
                                    ),
                                    RoundedCornerShape(26.dp)
                                )
                                .shadow(24.dp, RoundedCornerShape(26.dp))
                                .padding(24.dp)
                                .testTag("welcomeS2_feature_card")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Eyebrow with glowing indicator dot
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF38BDF8))
                                    )
                                    Text(
                                        text = "FEATURES",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8),
                                        letterSpacing = 2.2.sp
                                    )
                                }

                                // Headline: Your taste, your rules.
                                Text(
                                    text = buildAnnotatedString {
                                        append("Your taste, ")
                                        withStyle(
                                            SpanStyle(
                                                fontStyle = FontStyle.Italic,
                                                color = VeyraAmber,
                                                fontWeight = FontWeight.Bold
                                            )
                                        ) {
                                            append("your rules.")
                                        }
                                    },
                                    fontSize = 27.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeyraWhite,
                                    letterSpacing = (-0.6).sp,
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Shape every pick with the filters that matter.",
                                    fontSize = 13.5.sp,
                                    color = VeyraTextSecondary,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 19.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Feature Chips (welcomeChips)
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    WELCOME_FEATURE_CHIPS.forEach { chipLabel ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(VeyraCardElevated)
                                                .border(1.dp, VeyraBorder, RoundedCornerShape(10.dp))
                                                .padding(horizontal = 11.dp, vertical = 7.dp)
                                        ) {
                                            Text(
                                                text = chipLabel,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = VeyraWhite,
                                                letterSpacing = 0.3.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // SCREEN 3: Surprise pick card with clean movie poster (no placeholder text)
                    else -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("welcomeS3_surprise_screen")
                        ) {
                            // Title: Let Vera choose your next film.
                            Text(
                                text = buildAnnotatedString {
                                    append("Let ")
                                    withStyle(
                                        SpanStyle(
                                            fontStyle = FontStyle.Italic,
                                            color = Color(0xFF38BDF8),
                                            fontWeight = FontWeight.Bold
                                        )
                                    ) {
                                        append("Vera")
                                    }
                                    append(" choose your next film.")
                                },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                letterSpacing = (-0.6).sp,
                                textAlign = TextAlign.Center
                            )

                            // Glass Poster Card: pcard glass with volumetric glow and smooth 3D flip
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        rotationY = surpriseFlipRotation.value
                                        scaleX = surpriseScale.value
                                        scaleY = surpriseScale.value
                                        cameraDistance = 28f
                                    }
                                    .width(185.dp)
                                    .height(265.dp)
                                    .shadow(
                                        elevation = 28.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = Color(0x8038BDF8),
                                        spotColor = Color(0x600284C7)
                                    )
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(VeyraCard)
                                    .border(
                                        1.5.dp,
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x9038BDF8),
                                                Color(0x250284C7),
                                                Color.Transparent
                                            )
                                        ),
                                        RoundedCornerShape(18.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = SURPRISE_POSTERS[currentSurpriseIndex],
                                    contentDescription = "Movie Poster",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Controls: Dots Indicator & Actions
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dots: 3 indicators with active highlighted pill
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..3).forEach { dotIndex ->
                    val isDotActive = dotIndex == step
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(if (isDotActive) 22.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDotActive) Color(0xFF38BDF8) else Color(0x40FFFFFF)
                            )
                    )
                }
            }

            // Screen Action Buttons
            if (step < 3) {
                // Screen 1 & 2: Single "Continue" Button
                Button(
                    onClick = {
                        onPlayTactile()
                        onNext()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeyraWhite,
                        contentColor = VeyraBlack
                    ),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .height(52.dp)
                        .testTag("welcome_continue_button")
                ) {
                    Text(
                        text = "Continue",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                // Screen 3: Stack of 2 Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(0.88f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Secondary Button: Try a surprise pick (button.btn.g)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x351E293B),
                                        Color(0x180F172A)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x6038BDF8),
                                        Color(0x2038BDF8)
                                    )
                                ),
                                RoundedCornerShape(18.dp)
                            )
                            .clickable {
                                onPlayTactile()
                                coroutineScope.launch {
                                    // High-polish 3D Flip with depth compression
                                    launch {
                                        surpriseScale.animateTo(0.88f, tween(140, easing = FastOutSlowInEasing))
                                        surpriseScale.animateTo(1f, spring(dampingRatio = 0.70f, stiffness = 320f))
                                    }
                                    surpriseFlipRotation.animateTo(90f, tween(160, easing = FastOutSlowInEasing))
                                    currentSurpriseIndex = (currentSurpriseIndex + 1) % SURPRISE_POSTERS.size
                                    surpriseFlipRotation.snapTo(-90f)
                                    surpriseFlipRotation.animateTo(0f, spring(dampingRatio = 0.72f, stiffness = 340f))
                                }
                            }
                            .testTag("welcomeRoll"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Try a surprise pick",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE2E8F0)
                            )
                        }
                    }

                    // Primary Button: Get started (button#welcomeStartBtn)
                    Button(
                        onClick = {
                            onPlayTactile()
                            onFinish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeyraWhite,
                            contentColor = VeyraBlack
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("welcomeStartBtn")
                    ) {
                        Text(
                            text = "Get started",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
