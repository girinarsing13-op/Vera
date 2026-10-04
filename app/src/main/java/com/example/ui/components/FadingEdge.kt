package com.example.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a soft, continuous gradient alpha fade at the top edge of the screen.
 * Any content scrolling upward (including text, cards, and headers) smoothly dissolves:
 * 100% -> 70% -> 40% -> 15% -> 0%
 * with zero hard lines, zero clipping, and no rectangular boundary.
 */
fun Modifier.topFadingEdge(fadeHeight: Dp = 56.dp): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val fadePx = fadeHeight.toPx()
        drawRect(
            brush = Brush.verticalGradient(
                0.0f to Color.Transparent,
                0.25f to Color.Black.copy(alpha = 0.25f),
                0.6f to Color.Black.copy(alpha = 0.7f),
                1.0f to Color.Black,
                startY = 0f,
                endY = fadePx
            ),
            blendMode = BlendMode.DstIn
        )
    }
