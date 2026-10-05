package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraWhite
import kotlin.random.Random

private data class CelestialMote(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val alpha: Float,
    val speed: Float
)

/**
 * Returning User Small Welcome (Screen 0)
 *
 * Polished cinematic opening:
 * - Floating celestial motes drifting with subtle parallax
 * - Volumetric breathing ambient glow
 * - Horizontal cinematic anamorphic light flare
 * - Elegant letter-spaced Vera branding with subtle floating breath
 * - Laser-smooth glowing progress bar
 */
@Composable
fun BrandIntroOverlay(
    phrase: String = "Welcome back",
    modifier: Modifier = Modifier
) {
    val barProgress = remember { Animatable(0f) }
    val entranceAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        entranceAlpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        barProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
        )
    }

    // Stable particles pre-allocated once
    val motes = remember {
        val rand = Random(42)
        List(32) {
            CelestialMote(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = 1.0f + rand.nextFloat() * 1.8f,
                alpha = 0.25f + rand.nextFloat() * 0.55f,
                speed = 0.6f + rand.nextFloat() * 0.8f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "brand_intro_motion")
    val driftPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "drift_phase"
    )

    val breathingGlow by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_glow"
    )

    val streakPulse by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "streak_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
            .testTag("welcomeS0"),
        contentAlignment = Alignment.Center
    ) {
        // Celestial Stars & Ambient Light Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Ambient Volumetric Glow behind center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x550284C7),
                        Color(0x221E3A8A),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.46f),
                    radius = (w * 0.52f) * breathingGlow
                ),
                radius = (w * 0.52f) * breathingGlow,
                center = Offset(w * 0.5f, h * 0.46f)
            )

            // 2. Subtle Cinematic Anamorphic Horizontal Flare
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color(0x1538BDF8).copy(alpha = 0.20f * streakPulse),
                        Color(0x6038BDF8).copy(alpha = 0.55f * streakPulse),
                        Color(0x1538BDF8).copy(alpha = 0.20f * streakPulse),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(0f, h * 0.46f - 1.5f),
                size = Size(w, 3f)
            )

            // 3. Drifting Celestial Motes with Horizontal Parallax
            motes.forEach { mote ->
                val curX = ((mote.xRatio + driftPhase * mote.speed * 0.15f) % 1f) * w
                val curY = mote.yRatio * h
                drawCircle(
                    color = Color.White.copy(alpha = mote.alpha * entranceAlpha.value),
                    radius = mote.radius,
                    center = Offset(curX, curY)
                )
            }
        }

        // Center Content with Hardware Accelerated Graphics Layer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(24.dp)
                .graphicsLayer {
                    alpha = entranceAlpha.value
                    translationY = (1f - entranceAlpha.value) * 16f
                }
        ) {
            // Compact Logo: <div class="mlogo">Vera</div>
            Text(
                text = "Vera",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = VeyraWhite,
                letterSpacing = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .testTag("mlogo")
                    .graphicsLayer {
                        // Subtle breathing float in 3D space
                        translationY = (breathingGlow - 1f) * 4f
                    }
            )

            // Greeting Pill: <div class="hi glass"><span id="welcomeGreet">Welcome back</span></div>
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x351E293B),
                                Color(0x1A0F172A)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            listOf(
                                Color(0x6038BDF8),
                                Color(0x1538BDF8)
                            )
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 9.dp)
                    .testTag("welcomeGreet")
            ) {
                Text(
                    text = phrase.ifBlank { "Welcome back" },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFE2E8F0),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Progress Bar: <div class="bar"><i></i></div>
            Box(
                modifier = Modifier
                    .width(170.dp)
                    .height(3.5.dp)
                    .clip(CircleShape)
                    .background(Color(0x25FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(barProgress.value)
                        .height(3.5.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0284C7),
                                    Color(0xFF38BDF8),
                                    Color(0xFF93C5FD)
                                )
                            )
                        )
                )
            }
        }
    }
}
