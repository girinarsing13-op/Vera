package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VeyraBlack
import com.example.ui.theme.VeyraWhite

@Composable
fun HeaderBar(
    onNavigateHome: () -> Unit = {},
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sits naturally over the page with NO outer pill/box container
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT: Brand Logo & Title (integrated directly over page)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigateHome() }
                .padding(vertical = 4.dp)
                .testTag("brand_logo")
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(VeyraWhite),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "VEYRA",
                    tint = VeyraBlack,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 1.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "VEYRA",
                color = VeyraWhite,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
        }

        // RIGHT: ••• Menu / Settings Button (Frosted glass circular button)
        IconButton(
            onClick = onOpenSettings,
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x35FFFFFF))
                .border(1.dp, Color(0x28FFFFFF), CircleShape)
                .testTag("menu_settings_button")
        ) {
            Icon(
                imageVector = Icons.Default.MoreHoriz,
                contentDescription = "Settings",
                tint = VeyraWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
