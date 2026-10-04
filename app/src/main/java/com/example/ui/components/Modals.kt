package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DiscoveryConstants
import com.example.data.model.MediaItem
import com.example.ui.WatchlistCategory
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraBorderSubtle
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextPrimary
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite

@Composable
fun LanguageModal(
    selectedLanguage: String,
    onSelectLanguage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Language",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VeyraWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VeyraCardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VeyraTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                HorizontalDivider(color = VeyraBorderSubtle)

                // List
                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(DiscoveryConstants.LANGUAGES) { lang ->
                        val isSelected = selectedLanguage == lang.code
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color(0xFF71717A),
                            animationSpec = tween(180),
                            label = "lang_text"
                        )
                        val circleColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color(0xFF3F3F46),
                            animationSpec = tween(180),
                            label = "lang_circle"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectLanguage(lang.code) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = lang.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = circleColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = VeyraBorderSubtle)
                    }
                }
            }
        }
    }
}

@Composable
fun RegionModal(
    selectedRegion: String,
    onSelectRegion: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Region",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VeyraWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VeyraCardElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = VeyraTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                HorizontalDivider(color = VeyraBorderSubtle)

                LazyColumn(modifier = Modifier.heightIn(max = 380.dp)) {
                    items(DiscoveryConstants.REGIONS) { reg ->
                        val isSelected = selectedRegion == reg.code
                        val textColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color(0xFF71717A),
                            animationSpec = tween(180),
                            label = "reg_text"
                        )
                        val circleColor by animateColorAsState(
                            targetValue = if (isSelected) Color.White else Color(0xFF3F3F46),
                            animationSpec = tween(180),
                            label = "reg_circle"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectRegion(reg.code) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reg.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = textColor
                            )
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = circleColor,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                        }
                        HorizontalDivider(color = VeyraBorderSubtle)
                    }
                }
            }
        }
    }
}

@Composable
fun WatchlistModal(
    watchlist: List<MediaItem>,
    currentCategory: WatchlistCategory,
    onCategoryChanged: (WatchlistCategory) -> Unit,
    onViewItem: (MediaItem) -> Unit,
    onRemoveItem: (String) -> Unit,
    onPickRandom: () -> Unit,
    onDismiss: () -> Unit
) {
    val filteredList = when (currentCategory) {
        WatchlistCategory.ALL -> watchlist
        WatchlistCategory.MOVIE -> watchlist.filter { it.type == "Movie" }
        WatchlistCategory.SERIES -> watchlist.filter { it.type == "Series" }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with filter pills
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saved Watchlist",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VeyraWhite
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(VeyraCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = VeyraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Tabs
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(VeyraBlack)
                            .border(1.dp, VeyraBorder, RoundedCornerShape(12.dp))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            WatchlistCategory.ALL to "All",
                            WatchlistCategory.MOVIE to "Movies",
                            WatchlistCategory.SERIES to "Series"
                        ).forEach { (cat, label) ->
                            val isSelected = currentCategory == cat
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Color.Black else Color(0xFF71717A),
                                animationSpec = tween(180),
                                label = "watchlist_tab_text"
                            )
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else Color.Transparent,
                                animationSpec = tween(180),
                                label = "watchlist_tab_bg"
                            )
                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else Color.Transparent,
                                animationSpec = tween(180),
                                label = "watchlist_tab_border"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(backgroundColor)
                                    .border(if (isSelected) 1.dp else 0.dp, borderColor, RoundedCornerShape(8.dp))
                                    .clickable { onCategoryChanged(cat) }
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(color = VeyraBorderSubtle)

                // Content
                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = VeyraTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your watchlist is empty.\nSave something for later.",
                                fontSize = 12.sp,
                                color = VeyraTextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredList, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraBlack)
                                    .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.poster,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 62.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VeyraWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.year} • ${item.type}",
                                        fontSize = 11.sp,
                                        color = VeyraTextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onViewItem(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VeyraWhite,
                                        contentColor = VeyraBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 10.dp,
                                        vertical = 0.dp
                                    )
                                ) {
                                    Text(text = "View", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                IconButton(
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = VeyraTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = VeyraBorderSubtle)

                // Bottom actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onPickRandom,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeyraCardElevated,
                            contentColor = VeyraWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, VeyraBorder, RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = VeyraAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Pick Randomly", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeyraWhite,
                            contentColor = VeyraBlack
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "Done", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryModal(
    historyList: List<MediaItem>,
    onViewItem: (MediaItem) -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Discovered",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VeyraWhite
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (historyList.isNotEmpty()) {
                            Button(
                                onClick = onClearHistory,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VeyraCardElevated,
                                    contentColor = VeyraTextSecondary
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .border(1.dp, VeyraBorder, RoundedCornerShape(8.dp)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(text = "Clear", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(VeyraCardElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = VeyraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = VeyraBorderSubtle)

                if (historyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VeyraTextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No discovery history yet.",
                                fontSize = 12.sp,
                                color = VeyraTextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 350.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList, key = { it.id }) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraBlack)
                                    .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(14.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.poster,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(width = 44.dp, height = 62.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VeyraWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.year} • ${item.type}",
                                        fontSize = 11.sp,
                                        color = VeyraTextMuted
                                    )
                                }
                                Button(
                                    onClick = { onViewItem(item) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = VeyraWhite,
                                        contentColor = VeyraBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(30.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                        horizontal = 12.dp,
                                        vertical = 0.dp
                                    )
                                ) {
                                    Text(text = "View", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = VeyraBorderSubtle)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VeyraCardElevated,
                            contentColor = VeyraWhite
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.border(1.dp, VeyraBorder, RoundedCornerShape(12.dp))
                    ) {
                        Text(text = "Done", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun WhyThisModal(
    item: MediaItem,
    activeEra: String,
    activePickMode: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VeyraCardElevated)
                        .border(1.dp, VeyraBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = VeyraWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Why Veyra Picked This",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VeyraWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(VeyraBlack)
                        .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(14.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "VEYRA picked \"${item.title}\" because it matches your selected active filters:",
                        fontSize = 12.sp,
                        color = VeyraTextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "• Genres: ${item.genres.joinToString(", ")}", fontSize = 12.sp, color = VeyraWhite)
                    Text(text = "• Era: ${DiscoveryConstants.getEraCategory(item.year)}", fontSize = 12.sp, color = VeyraWhite)
                    Text(text = "• Platform: ${item.platforms.joinToString(", ")}", fontSize = 12.sp, color = VeyraWhite)
                    Text(text = "• Discovery Mode: $activePickMode", fontSize = 12.sp, color = VeyraWhite)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeyraWhite,
                        contentColor = VeyraBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Got it", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun NoResultsModal(
    onAdjustFilters: () -> Unit,
    onSurpriseMe: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(VeyraCard)
                .border(1.dp, VeyraBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(VeyraCardElevated)
                        .border(1.dp, VeyraBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = VeyraWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No titles found.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VeyraWhite
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "No titles found for these active filter combinations. Try adjusting your filters or let Veyra surprise you.",
                    fontSize = 12.sp,
                    color = VeyraTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onAdjustFilters,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeyraWhite,
                        contentColor = VeyraBlack
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Adjust Filters", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onSurpriseMe,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VeyraCardElevated,
                        contentColor = VeyraWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, VeyraBorder, RoundedCornerShape(12.dp))
                ) {
                    Text(text = "Surprise Me", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = VeyraTextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Dismiss", fontSize = 12.sp)
                }
            }
        }
    }
}
