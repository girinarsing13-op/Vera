package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreHoriz
import android.widget.Toast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraBorderSubtle
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraGold
import com.example.ui.theme.VeyraRed
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextPrimary
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite
import com.example.util.IntentHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultScreen(
    item: MediaItem?,
    isSavedToWatchlist: Boolean,
    isAlreadySeen: Boolean = false,
    isNotInterested: Boolean = false,
    onBackToFilters: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onDismissItem: () -> Unit,
    onSpinAgain: () -> Unit,
    onOpenWhyThis: () -> Unit,
    onMarkAsSeen: () -> Unit = {},
    onNotInterested: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    if (item == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(VeyraBlack),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = onBackToFilters) {
                Text("Return to Filters")
            }
        }
        return
    }

    // Cinematic Discovery Reveal
    val revealProgress = remember { Animatable(0f) }

    LaunchedEffect(item.id) {
        revealProgress.snapTo(0f)
        revealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing)
        )
    }

    val posterStage = (revealProgress.value / 0.65f).coerceIn(0f, 1f)
    val metadataStage = ((revealProgress.value - 0.20f) / 0.55f).coerceIn(0f, 1f)
    val actionsStage = ((revealProgress.value - 0.40f) / 0.60f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // Detail Top Bar: ← on the left, ••• on the right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .graphicsLayer { alpha = revealProgress.value },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(VeyraCardElevated)
                        .border(1.dp, VeyraBorderSubtle, CircleShape)
                        .clickable { onBackToFilters() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                        .testTag("back_to_filters_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = VeyraWhite,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Back",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = VeyraWhite
                    )
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(VeyraCardElevated)
                        .border(1.dp, VeyraBorderSubtle, CircleShape)
                        .testTag("detail_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Settings",
                        tint = VeyraWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Result Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(VeyraCard)
                    .border(1.dp, VeyraBorder, RoundedCornerShape(28.dp))
                    .testTag("result_card")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Poster Frame (Full uncropped aspect-ratio preserving fit)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2f / 3f)
                            .background(VeyraBlack)
                            .graphicsLayer {
                                alpha = posterStage
                                scaleX = 0.95f + 0.05f * posterStage
                                scaleY = 0.95f + 0.05f * posterStage
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = item.poster,
                            contentDescription = item.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Subtle cinematic light sweep sheen during discovery reveal
                        if (posterStage < 0.98f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color(0x3538BDF8).copy(alpha = (1f - posterStage) * 0.35f),
                                                Color.Transparent
                                            ),
                                            startX = posterStage * 900f - 300f,
                                            endX = posterStage * 900f + 250f
                                        )
                                    )
                            )
                        }

                        // Rating Pill
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.dp, VeyraBorder, CircleShape)
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = VeyraGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", item.rating),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite
                            )
                        }

                        // Type Badge
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .clip(CircleShape)
                                .background(VeyraWhite)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (item.type == "Movie") Icons.Default.Movie else Icons.Default.Tv,
                                contentDescription = null,
                                tint = VeyraBlack,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (item.type == "Movie") "MOVIE" else "TV SERIES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraBlack,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Content Details Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .graphicsLayer {
                                alpha = metadataStage
                                translationY = (1f - metadataStage) * 16f
                            },
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Metadata Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.year.toString(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VeyraWhite
                            )
                            Text(text = "•", fontSize = 12.sp, color = VeyraTextMuted)
                            Text(
                                text = item.duration,
                                fontSize = 12.sp,
                                color = VeyraTextSecondary
                            )
                            Text(text = "•", fontSize = 12.sp, color = VeyraTextMuted)
                            Text(
                                text = "${item.originalLanguage.uppercase()} / ${item.country ?: "GLOBAL"}",
                                fontSize = 12.sp,
                                color = VeyraTextSecondary
                            )

                            if (item.trending) {
                                Spacer(modifier = Modifier.weight(1f))
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(VeyraCardElevated)
                                        .border(1.dp, VeyraBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalFireDepartment,
                                        contentDescription = null,
                                        tint = VeyraRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "Trending",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VeyraTextPrimary
                                    )
                                }
                            }
                        }

                        // Title
                        Text(
                            text = item.title,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = VeyraWhite,
                            letterSpacing = (-0.5).sp
                        )

                        // Genres
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item.genres.forEach { genre ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(VeyraCardElevated)
                                        .border(1.dp, VeyraBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = VeyraTextPrimary
                                    )
                                }
                            }
                        }

                        // Synopsis
                        Text(
                            text = item.synopsis,
                            fontSize = 13.sp,
                            color = VeyraTextSecondary,
                            lineHeight = 20.sp
                        )

                        // Why Veyra Picked This Section
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Why Vera Picked This",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VeyraTextMuted,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Details",
                                    fontSize = 11.sp,
                                    color = VeyraTextSecondary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable { onOpenWhyThis() }
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    item.genres.firstOrNull() ?: "Curated",
                                    if (item.rating >= 8.0) "⭐ High Rating" else "✨ Recommended",
                                    item.platforms.firstOrNull() ?: "Streaming"
                                ).forEach { tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(VeyraBlack)
                                            .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = VeyraTextMuted
                                        )
                                    }
                                }
                            }
                        }

                        // Cast & Director Credits
                        if (item.director != null || item.cast.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (item.director != null) {
                                    Text(
                                        text = "Director: ${item.director}",
                                        fontSize = 12.sp,
                                        color = VeyraTextSecondary
                                    )
                                }
                                if (item.cast.isNotEmpty()) {
                                    Text(
                                        text = "Cast: ${item.cast.joinToString(", ")}",
                                        fontSize = 12.sp,
                                        color = VeyraTextSecondary
                                    )
                                }
                            }
                        }

                        // TV Series Season Breakdown
                        if (item.type == "Series" && item.seasons.isNotEmpty()) {
                            HorizontalDivider(color = VeyraBorderSubtle)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "SEASONS & EPISODES",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VeyraTextMuted,
                                    letterSpacing = 1.sp
                                )
                                item.seasons.forEach { season ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(VeyraBlack)
                                            .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = season.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = VeyraWhite
                                            )
                                            Text(
                                                text = "${season.episodeCount} Episodes",
                                                fontSize = 10.sp,
                                                color = VeyraTextMuted
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(VeyraCardElevated)
                                                .border(1.dp, VeyraBorder, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 8.dp, vertical = 3.dp)
                                        ) {
                                            Text(
                                                text = "Available",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = VeyraTextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = VeyraBorderSubtle)

                        // WATCH ON (Watch Providers)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WATCH ON",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VeyraWhite,
                                    letterSpacing = 1.sp
                                )
                                if (item.watchProviders.isNotEmpty()) {
                                    Text(
                                        text = "India (IN)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = VeyraTextMuted
                                    )
                                }
                            }

                            if (item.watchProviders.isNotEmpty()) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    item.watchProviders.forEach { provider ->
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(VeyraCardElevated)
                                                .border(1.dp, VeyraBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    IntentHelper.openStreamingPlatform(
                                                        context = context,
                                                        platform = provider.name,
                                                        title = item.title,
                                                        mediaType = item.type,
                                                        directWebLink = provider.link ?: item.watchLink
                                                    )
                                                }
                                                .padding(horizontal = 10.dp, vertical = 7.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (!provider.logoUrl.isNullOrBlank()) {
                                                AsyncImage(
                                                    model = provider.logoUrl,
                                                    contentDescription = provider.name,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .clip(RoundedCornerShape(5.dp))
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = null,
                                                    tint = VeyraWhite,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                            }
                                            Text(
                                                text = provider.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = VeyraWhite
                                            )
                                            if (provider.type != "Stream") {
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(VeyraBlack)
                                                        .border(0.5.dp, VeyraBorderSubtle, RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                                ) {
                                                    Text(
                                                        text = provider.type,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = VeyraTextMuted
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Fallback option: Search on Google / Chrome with exact title, year and media type
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(VeyraCardElevated)
                                        .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                                        .clickable {
                                            val typeTerm = if (item.type.equals("Series", ignoreCase = true)) "series" else "movie"
                                            val query = "${item.title} ${item.year} $typeTerm"
                                            val encodedQuery = try {
                                                URLEncoder.encode(query, "UTF-8")
                                            } catch (_: Exception) {
                                                query.replace(" ", "+")
                                            }
                                            val searchUrl = "https://www.google.com/search?q=$encodedQuery"
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUrl)).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                                Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(VeyraBlack)
                                                .border(1.dp, VeyraBorderSubtle, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Search on Google / Chrome",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = VeyraWhite
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${item.title} (${item.year})",
                                                fontSize = 11.sp,
                                                color = VeyraTextSecondary
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = VeyraTextMuted,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        // Bottom Actions: Exact 2-Row Pill Layout from Screenshot
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .graphicsLayer {
                                    alpha = actionsStage
                                    translationY = (1f - actionsStage) * 20f
                                },
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val primaryProvider = item.watchProviders.firstOrNull { it.type == "Stream" } ?: item.watchProviders.firstOrNull()
                            val primaryPlatformName = primaryProvider?.name ?: item.platforms.firstOrNull() ?: "Streaming"

                            // Row 1: Watch Now, Watchlist, Already Seen
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Watch Now (Pure White Pill)
                                Button(
                                    onClick = {
                                        IntentHelper.openStreamingPlatform(
                                            context = context,
                                            platform = primaryPlatformName,
                                            title = item.title,
                                            mediaType = item.type,
                                            directWebLink = primaryProvider?.link ?: item.watchLink
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(percent = 50),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1.08f)
                                        .height(44.dp)
                                        .testTag("watch_now_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Watch Now",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        maxLines = 1
                                    )
                                }

                                // 2. Watchlist (Dark Pill)
                                Button(
                                    onClick = onToggleWatchlist,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1C1C1F),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(percent = 50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(0.96f)
                                        .height(44.dp)
                                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(percent = 50))
                                        .testTag("watchlist_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isSavedToWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isSavedToWatchlist) "Saved" else "Watchlist",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }

                                // 3. Already Seen (Dark Pill)
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Got it.", Toast.LENGTH_SHORT).show()
                                        onMarkAsSeen()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1C1C1F),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(percent = 50),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1.06f)
                                        .height(44.dp)
                                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(percent = 50))
                                        .testTag("already_seen_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isAlreadySeen) "✓ Seen" else "Already Seen",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Row 2: Not Interested, Spin
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 1. Not Interested (Dark Pill with muted text)
                                Button(
                                    onClick = {
                                        Toast.makeText(context, "Not Interested", Toast.LENGTH_SHORT).show()
                                        onNotInterested()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1C1C1F),
                                        contentColor = Color(0xFFA1A1AA)
                                    ),
                                    shape = RoundedCornerShape(percent = 50),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(percent = 50))
                                        .testTag("not_interested_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "Not Interested",
                                        tint = Color(0xFFA1A1AA),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Not Interested",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFA1A1AA),
                                        maxLines = 1
                                    )
                                }

                                // 2. Spin (Dark Pill)
                                Button(
                                    onClick = onSpinAgain,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1C1C1F),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(percent = 50),
                                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .height(44.dp)
                                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(percent = 50))
                                        .testTag("spin_again_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shuffle,
                                        contentDescription = "Spin",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Spin",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
