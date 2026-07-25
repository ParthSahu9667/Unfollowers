package com.unfollowlens.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unfollowlens.ui.theme.AccentPrimary
import com.unfollowlens.ui.theme.BgSurfaceElevated
import com.unfollowlens.ui.theme.TextPrimary
import com.unfollowlens.ui.theme.TextSecondary

/**
 * User list item — neumorphic card row for displaying a username
 * with initials avatar, accent dot, subtext, and deep-link icon.
 *
 * @param username The Instagram username
 * @param subtitle Secondary text (e.g. "since Jul 2")
 * @param accentColor Category color (coral/blue/mint)
 * @param onClick Action when tapped (typically deep link to Instagram)
 */
@Composable
fun UserListItem(
    username: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    accentColor: Color = AccentPrimary,
    onClick: (() -> Unit)? = null
) {
    NeuCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        accentColor = accentColor,
        cornerRadius = 20.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Initials avatar
            InitialsAvatar(
                username = username,
                accentColor = accentColor
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Username + subtitle
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Deep link chevron
            if (onClick != null) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "Open Instagram profile",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Circular initials avatar — displays the first 1-2 characters of a username
 * with a soft inset shadow effect on a tinted background.
 */
@Composable
fun InitialsAvatar(
    username: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 44
) {
    val initials = username.take(2).uppercase()

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f))
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = accentColor
        )
    }
}
