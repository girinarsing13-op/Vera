package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.HeaderBar
import com.example.data.model.MediaItem
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
fun SearchScreen(
    query: String,
    results: List<MediaItem>,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSelectMedia: (MediaItem) -> Unit,
    onNavigateHome: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val quickSuggestions = listOf("Christopher Nolan", "Sci-Fi", "Interstellar", "Breaking Bad", "Anime", "Denis Villeneuve", "Thriller")

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

            // Search Bar Input Field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(VeyraCard)
                    .border(1.dp, VeyraBorder, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = VeyraTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = query,
                        onValueChange = onQueryChanged,
                        placeholder = {
                            Text(
                                text = "Search movies, TV, actors, directors...",
                                color = VeyraTextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            cursorColor = VeyraWhite,
                            focusedTextColor = VeyraWhite,
                            unfocusedTextColor = VeyraWhite,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_input_field")
                    )
                    if (isSearching) {
                        CircularProgressIndicator(
                            color = VeyraWhite,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClearQuery,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = VeyraTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick suggestion pills when search is empty
            if (query.isEmpty()) {
                Text(
                    text = "Suggestions",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = VeyraTextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickSuggestions) { suggestion ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(VeyraCardElevated)
                                .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(20.dp))
                                .clickable { onQueryChanged(suggestion) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                color = VeyraWhite,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-Time Results & Empty States
            if (query.isNotEmpty() && results.isEmpty()) {
                if (isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Searching titles for \"$query\"...",
                            fontSize = 14.sp,
                            color = VeyraTextSecondary
                        )
                    }
                } else {
                    // Empty state for no results
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(VeyraCardElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = VeyraTextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No results found for \"$query\"",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VeyraWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Try searching by another title, actor, or director.",
                                fontSize = 12.sp,
                                color = VeyraTextSecondary
                            )
                        }
                    }
                }
            } else {
                // Search Results Grid (2 Columns, Large Cinematic Posters)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 120.dp)
                ) {
                    items(results, key = { it.id }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onSelectMedia(item) }
                                .testTag("search_card_${item.id}")
                        ) {
                            // Large Vertical Poster
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(235.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(VeyraCardElevated)
                                    .border(1.dp, VeyraBorderSubtle, RoundedCornerShape(14.dp))
                            ) {
                                if (item.poster.isNotBlank()) {
                                    AsyncImage(
                                        model = item.poster,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF1B1B22)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Movie,
                                            contentDescription = null,
                                            tint = VeyraTextSecondary,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                // Top subtle scrim for badge legibility
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0x99000000),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                // Small "MOVIE" or "SERIES" badge on top-left
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopStart)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xD90E0E12))
                                        .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = item.type.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp,
                                        color = VeyraWhite
                                    )
                                }

                                // Rating Badge on top-right
                                if (item.rating > 0.0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xD90E0E12))
                                            .border(0.5.dp, Color(0x33FFFFFF), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = VeyraAmber,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = String.format("%.1f", item.rating),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = VeyraWhite
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Title Underneath
                            Text(
                                text = item.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VeyraWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Year & optional Genre
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.year.toString(),
                                    fontSize = 12.sp,
                                    color = VeyraTextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                if (item.genres.isNotEmpty()) {
                                    Text(
                                        text = "•",
                                        fontSize = 10.sp,
                                        color = VeyraTextMuted
                                    )
                                    Text(
                                        text = item.genres.first(),
                                        fontSize = 12.sp,
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
}
