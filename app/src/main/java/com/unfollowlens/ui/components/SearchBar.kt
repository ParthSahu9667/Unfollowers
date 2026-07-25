package com.unfollowlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.*

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search usernames\u2026"
) {
    val shape = RoundedCornerShape(16.dp)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = TextStyle(color = TextPrimary, fontSize = MaterialTheme.typography.bodyLarge.fontSize),
        cursorBrush = SolidColor(TextPrimary),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().height(52.dp).clip(shape)
                    .background(BgSurfaceElevated).border(1.dp, StrokeHairline, shape)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Search, "Search", tint = TextSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (query.isEmpty()) Text(placeholder, style = MaterialTheme.typography.bodyLarge, color = TextSecondary.copy(alpha = 0.6f))
                    innerTextField()
                }
            }
        },
        modifier = modifier
    )
}
