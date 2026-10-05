package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HeaderBar
import com.example.data.model.DiscoveryConstants
import com.example.data.repository.CuratedCatalogData
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraBorderSubtle
import com.example.data.model.MediaItem
import com.example.ui.components.MotionPosterCarousel
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextPrimary
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WelcomeScreen(
    selectedGenres: Set<String>,
    selectedEra: String,
    selectedPlatforms: Set<String>,
    selectedPickMode: String,
    selectedMood: String?,
    currentTypeFilter: String,
    selectedRegion: String,
    selectedLanguage: String,
    onToggleGenre: (String) -> Unit,
    onToggleAllGenres: (Boolean) -> Unit,
    onSetEra: (String) -> Unit,
    onTogglePlatform: (String) -> Unit,
    onToggleAllPlatforms: (Boolean) -> Unit,
    onSetPickMode: (String) -> Unit,
    onSetMood: (String) -> Unit,
    onClearMood: () -> Unit,
    onSetTypeFilter: (String) -> Unit,
    onOpenRegionModal: () -> Unit,
    onOpenLanguageModal: () -> Unit,
    onPickWatchlist: () -> Unit,
    onRandomizePick: () -> Unit,
    onOpenSettings: () -> Unit = {},
    motionPosters: List<MediaItem> = emptyList(),
    onSelectMedia: (MediaItem) -> Unit = {},
    onPlayTick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val regionLabel = DiscoveryConstants.REGIONS.find { it.code == selectedRegion }?.label ?: selectedRegion
    val languageLabel = DiscoveryConstants.LANGUAGES.find { it.code == selectedLanguage }?.label ?: selectedLanguage

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderBar(
                onNavigateHome = {},
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Hero Title & Subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Find your next film.",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = VeyraWhite,
                    letterSpacing = (-0.8).sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Curate your preferences by region, language, genres, eras, streaming services, and discovery modes. Powered by a live global cinematic catalog.",
                    fontSize = 13.sp,
                    color = VeyraTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3D Motion Poster Carousel with staggered entrance animation (Guaranteed rich showcase)
            val showcaseList = if (motionPosters.isNotEmpty()) motionPosters else CuratedCatalogData.getShowcaseMedia()
            MotionPosterCarousel(
                items = showcaseList,
                onSelectMedia = onSelectMedia,
                onPlayTick = onPlayTick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            // Main Configuration Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(VeyraCard)
                    .border(1.dp, VeyraBorder, RoundedCornerShape(28.dp))
                    .padding(20.dp)
                    .testTag("configuration_card")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    // 1. Region, Language & Format
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Region Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "REGION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VeyraTextMuted,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraCardElevated)
                                    .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                                    .clickable { onOpenRegionModal() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("region_selector_button"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = regionLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = VeyraWhite
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = VeyraTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Language Selector
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "LANGUAGE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VeyraTextMuted,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraCardElevated)
                                    .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                                    .clickable { onOpenLanguageModal() }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                                    .testTag("language_selector_button"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = languageLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = VeyraWhite
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = VeyraTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Format Segmented Bar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "FORMAT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VeyraTextMuted,
                                letterSpacing = 1.sp
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraBlack)
                                    .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("all" to "All", "Movie" to "Movie", "Series" to "Series").forEach { (typeVal, label) ->
                                    val isSelected = currentTypeFilter.equals(typeVal, ignoreCase = true)
                                    val textColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.Black else Color(0xFF71717A),
                                        animationSpec = tween(180),
                                        label = "format_text"
                                    )
                                    val backgroundColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Transparent,
                                        animationSpec = tween(180),
                                        label = "format_bg"
                                    )
                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) Color.White else Color.Transparent,
                                        animationSpec = tween(180),
                                        label = "format_border"
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(backgroundColor)
                                            .border(if (isSelected) 1.dp else 0.dp, borderColor, RoundedCornerShape(10.dp))
                                            .clickable { onSetTypeFilter(typeVal) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // 2. Genres
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GENRES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Select All",
                                    fontSize = 11.sp,
                                    color = if (selectedGenres.size == DiscoveryConstants.ALL_GENRES.size) Color.White else Color(0xFF71717A),
                                    fontWeight = if (selectedGenres.size == DiscoveryConstants.ALL_GENRES.size) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.clickable { onToggleAllGenres(true) }
                                )
                                Text(
                                    text = " • ",
                                    fontSize = 11.sp,
                                    color = VeyraBorder
                                )
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    color = if (selectedGenres.isEmpty()) Color.White else Color(0xFF71717A),
                                    fontWeight = if (selectedGenres.isEmpty()) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.clickable { onToggleAllGenres(false) }
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiscoveryConstants.ALL_GENRES.forEach { genre ->
                                FilterChipPill(
                                    text = genre,
                                    isSelected = selectedGenres.contains(genre),
                                    onClick = { onToggleGenre(genre) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // 3. Era
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ERA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Any Era",
                                fontSize = 11.sp,
                                color = if (selectedEra == "Any Era") Color.White else Color(0xFF71717A),
                                fontWeight = if (selectedEra == "Any Era") FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.clickable { onSetEra("Any Era") }
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiscoveryConstants.ALL_ERAS.forEach { era ->
                                FilterChipPill(
                                    text = era,
                                    isSelected = selectedEra == era,
                                    onClick = { onSetEra(era) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // 4. Streaming Platform
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STREAMING PLATFORM",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Any Platform",
                                    fontSize = 11.sp,
                                    color = if (selectedPlatforms.isEmpty() || selectedPlatforms.size == DiscoveryConstants.ALL_PLATFORMS.size) Color.White else Color(0xFF71717A),
                                    fontWeight = if (selectedPlatforms.isEmpty() || selectedPlatforms.size == DiscoveryConstants.ALL_PLATFORMS.size) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.clickable { onToggleAllPlatforms(true) }
                                )
                                Text(
                                    text = " • ",
                                    fontSize = 11.sp,
                                    color = VeyraBorder
                                )
                                Text(
                                    text = "Clear",
                                    fontSize = 11.sp,
                                    color = if (selectedPlatforms.isEmpty()) Color.White else Color(0xFF71717A),
                                    fontWeight = if (selectedPlatforms.isEmpty()) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.clickable { onToggleAllPlatforms(false) }
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiscoveryConstants.ALL_PLATFORMS.forEach { plat ->
                                FilterChipPill(
                                    text = plat,
                                    isSelected = selectedPlatforms.contains(plat),
                                    onClick = { onTogglePlatform(plat) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // 5. Discovery Mode
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "DISCOVERY MODE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VeyraWhite,
                            letterSpacing = 1.sp
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiscoveryConstants.PICK_MODES.forEach { mode ->
                                FilterChipPill(
                                    text = mode,
                                    isSelected = selectedPickMode == mode,
                                    onClick = { onSetPickMode(mode) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // 6. Mood Discovery
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "WHAT ARE YOU IN THE MOOD FOR?",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                letterSpacing = 1.sp
                            )
                            if (selectedMood != null) {
                                Text(
                                    text = "Clear Mood",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { onClearMood() }
                                )
                            }
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DiscoveryConstants.MOODS.forEach { mood ->
                                FilterChipPill(
                                    text = mood.label,
                                    isSelected = selectedMood == mood.id,
                                    onClick = { onSetMood(mood.id) }
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = VeyraBorderSubtle)

                    // Primary Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pick Watchlist
                        Button(
                            onClick = onPickWatchlist,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VeyraCardElevated,
                                contentColor = VeyraWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .border(1.dp, VeyraBorder, RoundedCornerShape(16.dp))
                                .testTag("pick_watchlist_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = VeyraWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Watchlist",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Randomize Pick (Primary Hero Action)
                        Button(
                            onClick = onRandomizePick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = VeyraWhite,
                                contentColor = VeyraBlack
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("randomize_pick_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = VeyraBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Randomize Pick",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Footer
            Text(
                text = "Vera • Minimalist Apple-inspired Cinematic Engine.",
                fontSize = 11.sp,
                color = VeyraTextMuted,
                textAlign = TextAlign.Center
            )
            // Extra bottom scroll clearance so content/buttons are completely above the floating navigation bar
            Spacer(modifier = Modifier.height(130.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

@Composable
fun FilterChipPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else Color(0xFF71717A),
        animationSpec = tween(durationMillis = 180),
        label = "chip_text_color"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF222226),
        animationSpec = tween(durationMillis = 180),
        label = "chip_border_color"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFF0C0C0E),
        animationSpec = tween(durationMillis = 180),
        label = "chip_bg_color"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = textColor
        )
    }
}

