package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.HeaderBar
import com.example.data.model.MediaItem
import com.example.ui.HistoryFilter
import com.example.ui.theme.VeyraAmber
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraBorder
import com.example.ui.theme.VeyraBorderSubtle
import com.example.ui.theme.VeyraCard
import com.example.ui.theme.VeyraCardElevated
import com.example.ui.theme.VeyraTextMuted
import com.example.ui.theme.VeyraTextSecondary
import com.example.ui.theme.VeyraWhite

@Composable
fun HistoryScreen(
    historyList: List<MediaItem>,
    currentFilter: HistoryFilter,
    onFilterChanged: (HistoryFilter) -> Unit,
    onViewItem: (MediaItem) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredList = when (currentFilter) {
        HistoryFilter.ALL -> historyList
        HistoryFilter.SEEN -> historyList.filter { it.status == "seen" }
        HistoryFilter.NOT_INTERESTED -> historyList.filter { it.status == "not_interested" }
        HistoryFilter.SAVED -> historyList.filter { it.status == "saved" || it.isSavedToWatchlist }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VeyraBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            HeaderBar(
                onNavigateHome = onNavigateHome,
                onOpenSettings = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // History Header & Clear Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "History & Memory",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VeyraWhite
                )

                if (historyList.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(VeyraCardElevated)
                            .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(10.dp))
                            .clickable { onClearHistory() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("clear_history_button"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear",
                            tint = VeyraTextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clear All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = VeyraTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented Filters: [ All ] [ Seen ] [ Not Interested ] [ Saved ]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VeyraCard)
                    .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(
                    HistoryFilter.ALL to "All (${historyList.size})",
                    HistoryFilter.SEEN to "Seen (${historyList.count { it.status == "seen" }})",
                    HistoryFilter.NOT_INTERESTED to "Not Interested (${historyList.count { it.status == "not_interested" }})",
                    HistoryFilter.SAVED to "Saved (${historyList.count { it.status == "saved" || it.isSavedToWatchlist }})"
                ).forEach { (filter, label) ->
                    val isSelected = currentFilter == filter
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.Black else Color(0xFF71717A),
                        animationSpec = tween(180),
                        label = "tab_text_${filter.name}"
                    )
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else Color.Transparent,
                        animationSpec = tween(180),
                        label = "tab_bg_${filter.name}"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(bgColor)
                            .clickable { onFilterChanged(filter) }
                            .padding(vertical = 7.dp)
                            .testTag("history_tab_${filter.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                // Minimal Empty State for Selected Filter
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VeyraCard)
                                .border(1.dp, VeyraBorderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = VeyraTextSecondary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = when (currentFilter) {
                                HistoryFilter.SEEN -> "No seen titles yet."
                                HistoryFilter.NOT_INTERESTED -> "No rejected titles."
                                HistoryFilter.SAVED -> "No saved items."
                                HistoryFilter.ALL -> "No history yet."
                            },
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = VeyraWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (currentFilter) {
                                HistoryFilter.SEEN -> "Titles marked as 'Already Seen' appear here."
                                HistoryFilter.NOT_INTERESTED -> "Titles marked 'Not Interested' appear here."
                                HistoryFilter.SAVED -> "Titles added to your Watchlist appear here."
                                HistoryFilter.ALL -> "Interactions with movies and shows will appear here."
                            },
                            fontSize = 13.sp,
                            color = VeyraTextSecondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp, top = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredList, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(VeyraCard)
                                .border(1.dp, VeyraBorder, RoundedCornerShape(16.dp))
                                .clickable { onViewItem(item) }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Poster
                            Box(
                                modifier = Modifier
                                    .size(width = 60.dp, height = 86.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(VeyraCardElevated)
                            ) {
                                if (item.poster.isNotBlank()) {
                                    AsyncImage(
                                        model = item.poster,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = VeyraTextSecondary,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = item.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VeyraWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(VeyraBlack)
                                            .border(0.5.dp, VeyraBorderSubtle, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = item.type,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = VeyraWhite
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.year.toString(),
                                        fontSize = 12.sp,
                                        color = VeyraTextSecondary
                                    )
                                    Text(
                                        text = " • ",
                                        fontSize = 12.sp,
                                        color = VeyraTextMuted
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = VeyraAmber,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = String.format("%.1f", item.rating),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VeyraWhite
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Clear Status Badge on each History Item
                                val (badgeText, badgeBg, badgeTextColor) = when {
                                    item.status == "seen" -> Triple("Already Seen", Color(0xFF142436), Color(0xFF93C5FD))
                                    item.status == "not_interested" -> Triple("Not Interested", Color(0xFF26262B), Color(0xFFA1A1AA))
                                    item.status == "saved" || item.isSavedToWatchlist -> Triple("Saved", Color(0xFF2D2311), Color(0xFFFDE047))
                                    else -> Triple("Discovered", Color(0xFF1E1E24), Color(0xFF71717A))
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .border(0.5.dp, badgeTextColor.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                            .testTag("status_badge_${item.id}")
                                    ) {
                                        Text(
                                            text = badgeText,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = badgeTextColor
                                        )
                                    }

                                    Text(
                                        text = item.genres.take(2).joinToString(" • "),
                                        fontSize = 10.sp,
                                        color = VeyraTextMuted,
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
}
