package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DiscoveryConstants
import com.example.data.model.MediaItem
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraWhite
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FloatingPosterSlot(
    val initialAngleRad: Float,
    val initialDistanceDp: Float,
    val baseScale: Float,
    val baseRotationZ: Float,
    val rotationY: Float,
    val phaseOffset: Float,
    val baseAlpha: Float
)

private data class MistParticle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

private data class CelestialStar(
    val xRatio: Float,
    val yRatio: Float,
    val speed: Float,
    val radius: Float,
    val color: Color
)

/**
 * Restored Cinematic Movie Search & Recommendation Reveal
 *
 * Sequence:
 * 1. FLOATING POSTERS: Candidate posters float smoothly through 3D space in varied directions
 * 2. POSTERS MOVE / CONVERGE: Motion narrows down and converges inward toward the center
 * 3. CATALOG NARROWS DOWN: Spatial possibilities collapse to a single focal point
 * 4. FROST / MIST / SMOKE BURST: Subtle, cinematic cryo-mist burst blooms around the choice
 * 5. FINAL MOVIE POSTER EMERGES: Target recommendation emerges from the mist with elastic spring
 * 6. RECOMMENDATION REVEAL: Final interface transition
 */
@Composable
fun WarpScreen(
    statusText: String,
    candidatePosters: List<String>,
    winnerItem: MediaItem?,
    isTargetLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    // Reliable candidate pool for floating posters
    val posters = remember(candidatePosters) {
        val pool = candidatePosters.filter { it.isNotBlank() }.ifEmpty { DiscoveryConstants.FALLBACK_POSTERS }
        val list = mutableListOf<String>()
        while (list.size < 8) {
            list.addAll(pool)
        }
        list.take(8)
    }

    // 8 Pre-configured 3D spatial trajectories converging toward center
    val slots = remember {
        val anglesDeg = listOf(25f, 70f, 120f, 165f, 210f, 255f, 300f, 345f)
        val distances = listOf(220f, 250f, 210f, 240f, 230f, 260f, 215f, 245f)
        val scales = listOf(0.70f, 0.65f, 0.75f, 0.68f, 0.72f, 0.66f, 0.74f, 0.68f)
        val rotationsZ = listOf(-7f, 6f, -5f, 8f, -6f, 5f, -8f, 7f)
        val rotationsY = listOf(-16f, 14f, -12f, 18f, -14f, 16f, -15f, 12f)
        val opacities = listOf(0.55f, 0.45f, 0.60f, 0.50f, 0.55f, 0.48f, 0.58f, 0.52f)

        List(8) { i ->
            FloatingPosterSlot(
                initialAngleRad = (anglesDeg[i] * (Math.PI / 180f)).toFloat(),
                initialDistanceDp = distances[i],
                baseScale = scales[i],
                baseRotationZ = rotationsZ[i],
                rotationY = rotationsY[i],
                phaseOffset = i * 0.85f,
                baseAlpha = opacities[i]
            )
        }
    }

    // Pre-allocated crystalline frost/mist motes for the discovery burst
    val mistParticles = remember {
        val rand = Random(777)
        List(22) {
            MistParticle(
                angle = rand.nextFloat() * 2f * Math.PI.toFloat(),
                speed = 0.6f + rand.nextFloat() * 0.9f,
                size = 2.0f + rand.nextFloat() * 3.5f,
                alpha = 0.4f + rand.nextFloat() * 0.5f
            )
        }
    }

    // Ambient stars in background
    val stars = remember {
        val rand = Random(888)
        val palette = listOf(
            Color(0xFFE0F2FE),
            Color(0xFF38BDF8),
            Color(0xFFBAE6FD),
            Color.White
        )
        List(42) {
            CelestialStar(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                speed = 0.5f + rand.nextFloat() * 1.0f,
                radius = 1.0f + rand.nextFloat() * 1.8f,
                color = palette[rand.nextInt(palette.size)]
            )
        }
    }

    // Continuous floating ambient motion
    val transition = rememberInfiniteTransition(label = "warp_floating_motion")

    val floatTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.2831855f, // 2*PI
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_time"
    )

    val reticlePulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reticle_pulse"
    )

    // Convergence, Frost/Mist Burst, and Winner Emergence animatables
    val convergenceProgress = remember { Animatable(0f) }
    val mistBurstProgress = remember { Animatable(0f) }
    val winnerScale = remember { Animatable(0.76f) }
    val winnerAlpha = remember { Animatable(0f) }

    LaunchedEffect(isTargetLocked) {
        if (isTargetLocked) {
            // Step 1: Floating posters converge and narrow down into center
            launch {
                convergenceProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing)
                )
            }

            // Step 2: Frost/Mist/Smoke burst blooms right at convergence
            launch {
                mistBurstProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                )
            }

            // Step 3: Final selected movie poster emerges from the mist
            launch {
                winnerAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                )
            }
            launch {
                winnerScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(dampingRatio = 0.72f, stiffness = 300f)
                )
            }
        } else {
            convergenceProgress.snapTo(0f)
            mistBurstProgress.snapTo(0f)
            winnerAlpha.snapTo(0f)
            winnerScale.snapTo(0.76f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
            .testTag("cinematic_warp_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Deep Atmospheric Background with Ambient Glow & Floating Stars
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            // Volumetric ambient core glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isTargetLocked) Color(0x650284C7) else Color(0x351E3A8A),
                        Color(0x150F172A),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = center
            )

            // Ambient background stars
            stars.forEach { star ->
                val floatOffsetX = sin(floatTime + star.xRatio * 5f) * 4f
                val curX = (star.xRatio * size.width + floatOffsetX).coerceIn(0f, size.width)
                val curY = star.yRatio * size.height
                drawCircle(
                    color = star.color.copy(alpha = 0.45f),
                    radius = star.radius,
                    center = Offset(curX, curY)
                )
            }

            // SUBTLE FROST / MIST / SMOKE BURST WHEN TARGET IS LOCKED
            if (mistBurstProgress.value > 0.01f && mistBurstProgress.value < 0.99f) {
                val burst = mistBurstProgress.value
                val mistAlpha = ((1f - burst) * 0.75f).coerceIn(0f, 1f)

                // Expanding ethereal mist vapor rings
                val maxRadius = size.width * 0.50f
                val currentMistRadius = (45.dp.toPx() + burst * (maxRadius - 45.dp.toPx()))

                // Layer 1: Core Frosted Cloud (Ice-blue / Translucent white)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x80BAE6FD).copy(alpha = 0.55f * mistAlpha),
                            Color(0x4038BDF8).copy(alpha = 0.35f * mistAlpha),
                            Color(0x150284C7).copy(alpha = 0.15f * mistAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = currentMistRadius
                    ),
                    radius = currentMistRadius,
                    center = center
                )

                // Layer 2: Soft Swirling Outer Vapor Mist
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x3038BDF8).copy(alpha = 0.28f * mistAlpha),
                            Color(0x10BAE6FD).copy(alpha = 0.10f * mistAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = currentMistRadius * 1.25f
                    ),
                    radius = currentMistRadius * 1.25f,
                    center = center
                )

                // Layer 3: Crystalline Frost Motes drifting outwards
                mistParticles.forEach { p ->
                    val dist = currentMistRadius * 0.85f * p.speed
                    val px = center.x + cos(p.angle) * dist
                    val py = center.y + sin(p.angle) * dist
                    drawCircle(
                        color = Color.White.copy(alpha = p.alpha * mistAlpha),
                        radius = p.size * (1f - burst * 0.3f),
                        center = Offset(px, py)
                    )
                }
            }
        }

        // FLOATING POSTERS IN 3D SPACE (Move smoothly and converge toward center)
        val convergence = convergenceProgress.value
        if (convergence < 0.98f) {
            posters.forEachIndexed { index, posterUrl ->
                val slot = slots.getOrElse(index) { slots.first() }

                // Smooth floating sinusoidal wobble in space
                val wobbleDist = sin(floatTime + slot.phaseOffset) * 9f
                val wobbleRot = cos(floatTime + slot.phaseOffset) * 1.8f

                // As convergence progresses, radius collapses to 0 (into the center)
                val currentDistanceDp = slot.initialDistanceDp * (1f - convergence)
                val transX = with(density) { (cos(slot.initialAngleRad) * currentDistanceDp).dp.toPx() }
                val transY = with(density) { (sin(slot.initialAngleRad) * currentDistanceDp + wobbleDist).dp.toPx() }

                // As posters converge, scale down slightly and dissolve into center
                val currentScale = slot.baseScale * (1f - 0.30f * convergence)
                val currentAlpha = (slot.baseAlpha * (1f - convergence)).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = transX
                            translationY = transY
                            scaleX = currentScale
                            scaleY = currentScale
                            rotationZ = slot.baseRotationZ + wobbleRot
                            rotationY = slot.rotationY
                            cameraDistance = 28f
                            alpha = currentAlpha
                        }
                        .size(width = 86.dp, height = 126.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shadow(16.dp, RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0x3538BDF8), RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // CENTER TARGETING PROJECTOR GATE & FINAL MOVIE POSTER EMERGENCE
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp, 290.dp)
            ) {
                // Convergence Targeting Reticle when Target is Locked
                if (isTargetLocked) {
                    // Outer Pulsing Reticle Ring
                    Box(
                        modifier = Modifier
                            .size(232.dp)
                            .graphicsLayer {
                                scaleX = reticlePulse
                                scaleY = reticlePulse
                            }
                            .border(
                                1.5.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0x9538BDF8),
                                        Color(0x2038BDF8),
                                        Color(0xFF38BDF8),
                                        Color(0x2038BDF8),
                                        Color(0x9538BDF8)
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Inner Concentric Pulse Ring
                    Box(
                        modifier = Modifier
                            .size(196.dp)
                            .border(1.dp, Color(0x3538BDF8), CircleShape)
                    )
                }

                // Final Recommendation Poster Emerging from the Collection / Mist
                if (winnerItem != null && winnerAlpha.value > 0.01f) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = winnerScale.value
                                scaleY = winnerScale.value
                                alpha = winnerAlpha.value
                                cameraDistance = 32f
                            }
                            .size(width = 168.dp, height = 252.dp)
                            .shadow(
                                elevation = 32.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color(0x9038BDF8),
                                spotColor = Color(0xC038BDF8)
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0A0A0C))
                            .border(
                                2.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF38BDF8),
                                        Color(0x600284C7),
                                        Color(0x1538BDF8)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                    ) {
                        AsyncImage(
                            model = winnerItem.poster,
                            contentDescription = winnerItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Status Pill with dynamic glowing border
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.85f))
                    .border(
                        1.dp,
                        if (isTargetLocked) {
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF38BDF8),
                                    Color(0xFF0284C7),
                                    Color(0xFF38BDF8)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    VeyraBorder,
                                    Color(0x5038BDF8),
                                    VeyraBorder
                                )
                            )
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (isTargetLocked) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                    }

                    Text(
                        text = statusText.uppercase(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTargetLocked) Color(0xFF38BDF8) else VeyraWhite,
                        letterSpacing = 1.6.sp
                    )
                }
            }
        }
    }
}
