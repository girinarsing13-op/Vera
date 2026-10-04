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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.LibraryView
import com.example.ui.WatchlistCategory
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
fun WatchlistScreen(
    watchlist: List<MediaItem>,
    seenList: List<MediaItem>,
    currentCategory: WatchlistCategory,
    currentLibraryView: LibraryView,
    onCategoryChanged: (WatchlistCategory) -> Unit,
    onLibraryViewChanged: (LibraryView) -> Unit,
    onViewItem: (MediaItem) -> Unit,
    onRemoveItem: (String) -> Unit,
    onRemoveFromSeen: (String) -> Unit,
    onPickRandom: () -> Unit,
    onExplore: () -> Unit,
    onNavigateHome: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredWatchlist = when (currentCategory) {
        WatchlistCategory.ALL -> watchlist
        WatchlistCategory.MOVIE -> watchlist.filter { it.type == "Movie" }
        WatchlistCategory.SERIES -> watchlist.filter { it.type == "Series" }
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

            // Dual Library Switcher: Saved Watchlist vs Already Seen
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(VeyraCard)
                    .border(1.dp, VeyraBorder, RoundedCornerShape(14.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Watchlist Option
                val isWatchlistSelected = currentLibraryView == LibraryView.WATCHLIST
                val wlTextColor by animateColorAsState(
                    targetValue = if (isWatchlistSelected) Color.Black else Color(0xFF71717A),
                    animationSpec = tween(180),
                    label = "wl_tab_text"
                )
                val wlBgColor by animateColorAsState(
                    targetValue = if (isWatchlistSelected) Color.White else Color.Transparent,
                    animationSpec = tween(180),
                    label = "wl_tab_bg"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(wlBgColor)
                        .clickable { onLibraryViewChanged(LibraryView.WATCHLIST) }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = wlTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Saved (${watchlist.size})",
                        fontSize = 12.sp,
                        fontWeight = if (isWatchlistSelected) FontWeight.Bold else FontWeight.Medium,
                        color = wlTextColor
                    )
                }

                // Already Seen Option
                val isSeenSelected = currentLibraryView == LibraryView.ALREADY_SEEN
                val seenTextColor by animateColorAsState(
                    targetValue = if (isSeenSelected) Color.Black else Color(0xFF71717A),
                    animationSpec = tween(180),
                    label = "seen_tab_text"
                )
                val seenBgColor by animateColorAsState(
                    targetValue = if (isSeenSelected) Color.White else Color.Transparent,
                    animationSpec = tween(180),
                    label = "seen_tab_bg"
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(seenBgColor)
                        .clickable { onLibraryViewChanged(LibraryView.ALREADY_SEEN) }
                        .padding(vertical = 8.dp)
                        .testTag("already_seen_tab"),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = seenTextColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Already Seen (${seenList.size})",
                        fontSize = 12.sp,
                        fontWeight = if (isSeenSelected) FontWeight.Bold else FontWeight.Medium,
                        color = seenTextColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentLibraryView == LibraryView.WATCHLIST) {
                // Category Pills & Spin Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(VeyraCard)
                            .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(10.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        listOf(
                            WatchlistCategory.ALL to "All",
                            WatchlistCategory.MOVIE to "Movies",
                            WatchlistCategory.SERIES to "Series"
                        ).forEach { (cat, label) ->
                            val isSelected = currentCategory == cat
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else Color(0xFF71717A),
                                animationSpec = tween(180),
                                label = "sub_tab_text"
                            )
                            val bgColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFF27272A) else Color.Transparent,
                                animationSpec = tween(180),
                                label = "sub_tab_bg"
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(bgColor)
                                    .clickable { onCategoryChanged(cat) }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                            }
                        }
                    }

                    // Random pick button if watchlist has items
                    if (filteredWatchlist.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(VeyraCardElevated)
                                .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(10.dp))
                                .clickable { onPickRandom() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("pick_random_watchlist_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = VeyraAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Spin",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = VeyraWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredWatchlist.isEmpty()) {
                    // Clean Minimal Empty State
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
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = VeyraTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Nothing saved yet.",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Save something for later from Discover or Search.",
                                fontSize = 13.sp,
                                color = VeyraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onExplore,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VeyraWhite,
                                    contentColor = VeyraBlack
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("explore_movies_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Explore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Discover Titles",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 120.dp, top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredWatchlist, key = { it.id }) { item ->
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
                                        IconButton(
                                            onClick = { onRemoveItem(item.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = VeyraTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

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
                                        Text(
                                            text = item.type,
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

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.genres.take(3).joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = VeyraTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // ALREADY SEEN SECTION
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Already Seen Collection",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VeyraWhite
                        )
                        Text(
                            text = "Filtered out of future recommendations",
                            fontSize = 11.sp,
                            color = VeyraTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (seenList.isEmpty()) {
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
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = VeyraTextSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "No titles marked seen yet.",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap \"✓ Already Seen\" on any recommendation to filter it from future spins.",
                                fontSize = 13.sp,
                                color = VeyraTextSecondary,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 120.dp, top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(seenList, key = { it.id }) { item ->
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

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.genres.take(3).joinToString(" • "),
                                        fontSize = 11.sp,
                                        color = VeyraTextMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    // Explicit "Remove from Seen" action
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(VeyraCardElevated)
                                            .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(8.dp))
                                            .clickable { onRemoveFromSeen(item.id) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("remove_from_seen_button"),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = VeyraTextSecondary,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Remove from Seen",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = VeyraTextSecondary
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
}
