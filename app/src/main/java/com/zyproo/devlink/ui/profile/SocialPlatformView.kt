package com.zyproo.devlink.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zyproo.devlink.data.remote.dto.SocialLinkResponse
import com.zyproo.devlink.data.remote.dto.SocialPlatform
import com.zyproo.devlink.ui.theme.*

fun getPlatformDrawableName(platform: SocialPlatform): String {
    return when (platform) {
        SocialPlatform.GITHUB -> "ic_github"
        SocialPlatform.LINKEDIN -> "ic_linkedin"
        SocialPlatform.LEETCODE -> "ic_leetcode"
        SocialPlatform.INSTAGRAM -> "ic_instagram"
        SocialPlatform.TWITTER -> "ic_twitter"
        SocialPlatform.YOUTUBE -> "ic_youtube"
        SocialPlatform.PORTFOLIO -> "ic_portfolio"
        SocialPlatform.CODEFORCES -> "ic_codeforces"
        SocialPlatform.HACKERRANK -> "ic_hackerrank"
        SocialPlatform.KAGGLE -> "ic_kaggle"
        SocialPlatform.MEDIUM -> "ic_medium"
        SocialPlatform.DEVTO -> "ic_devto"
        SocialPlatform.BEHANCE -> "ic_behance"
        SocialPlatform.DRIBBBLE -> "ic_dribbble"
    }
}

fun getPlatformMeta(platform: SocialPlatform): Pair<String, String> {
    return when (platform) {
        SocialPlatform.GITHUB -> "🐙" to "GitHub"
        SocialPlatform.LINKEDIN -> "💼" to "LinkedIn"
        SocialPlatform.LEETCODE -> "🧩" to "LeetCode"
        SocialPlatform.INSTAGRAM -> "📷" to "Instagram"
        SocialPlatform.TWITTER -> "🐦" to "Twitter / X"
        SocialPlatform.YOUTUBE -> "📹" to "YouTube"
        SocialPlatform.PORTFOLIO -> "🌐" to "Portfolio"
        SocialPlatform.CODEFORCES -> "🏆" to "Codeforces"
        SocialPlatform.HACKERRANK -> "💻" to "HackerRank"
        SocialPlatform.KAGGLE -> "📊" to "Kaggle"
        SocialPlatform.MEDIUM -> "📝" to "Medium"
        SocialPlatform.DEVTO -> "👩‍💻" to "Dev.to"
        SocialPlatform.BEHANCE -> "🎨" to "Behance"
        SocialPlatform.DRIBBBLE -> "🏀" to "Dribbble"
    }
}

fun getPlatformColor(platform: SocialPlatform): Color {
    return when (platform) {
        SocialPlatform.GITHUB -> Color(0xFF24292E)
        SocialPlatform.LINKEDIN -> Color(0xFF0A66C2)
        SocialPlatform.LEETCODE -> Color(0xFFFFA116)
        SocialPlatform.INSTAGRAM -> Color(0xFFE4405F)
        SocialPlatform.TWITTER -> Color(0xFF1DA1F2)
        SocialPlatform.YOUTUBE -> Color(0xFFFF0000)
        SocialPlatform.PORTFOLIO -> DevLinkBlue
        SocialPlatform.CODEFORCES -> Color(0xFF1F8ACB)
        SocialPlatform.HACKERRANK -> Color(0xFF2EC866)
        SocialPlatform.KAGGLE -> Color(0xFF20BEFF)
        SocialPlatform.MEDIUM -> Color(0xFF000000)
        SocialPlatform.DEVTO -> Color(0xFF0A0A0A)
        SocialPlatform.BEHANCE -> Color(0xFF1769FF)
        SocialPlatform.DRIBBBLE -> Color(0xFFEA4C89)
    }
}

@Composable
fun SocialPlatformIcon(
    platform: SocialPlatform,
    modifier: Modifier = Modifier.size(28.dp)
) {
    val context = LocalContext.current
    val resName = remember(platform) { getPlatformDrawableName(platform) }
    val resId = remember(resName) {
        context.resources.getIdentifier(resName, "drawable", context.packageName)
    }

    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = getPlatformMeta(platform).second,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        // High quality fallback with brand color badge and emoji/symbol
        val brandColor = getPlatformColor(platform)
        val meta = getPlatformMeta(platform)
        Box(
            modifier = modifier
                .clip(CircleShape)
                .background(brandColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = meta.first,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun SocialLinksSection(
    socialLinks: List<SocialLinkResponse>,
    isOwnProfile: Boolean = false,
    onAddClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (socialLinks.isEmpty() && !isOwnProfile) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DevLinkBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = DevLinkBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Social & Profiles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DevLinkTextPrimary
                    )
                }

                if (isOwnProfile && onAddClick != null && socialLinks.size < 5) {
                    TextButton(
                        onClick = onAddClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = DevLinkBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Manage",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DevLinkBlue
                        )
                    }
                }
            }

            if (socialLinks.isEmpty()) {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add your GitHub, LinkedIn, or portfolio links to highlight your work to peers and recruiters.",
                            fontSize = 12.sp,
                            color = DevLinkTextSecondary,
                            lineHeight = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (onAddClick != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = onAddClick,
                                colors = ButtonDefaults.buttonColors(containerColor = DevLinkBlue),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    socialLinks.forEach { link ->
                        val meta = getPlatformMeta(link.platform)
                        val displayUrl = link.url.removePrefix("https://").removePrefix("http://")

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val safeUrl = if (!link.url.startsWith("http://") && !link.url.startsWith("https://")) {
                                        "https://${link.url}"
                                    } else {
                                        link.url
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(safeUrl))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Cannot open URL: $safeUrl", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    SocialPlatformIcon(
                                        platform = link.platform,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = meta.second,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DevLinkTextPrimary
                                        )
                                        Text(
                                            text = displayUrl,
                                            fontSize = 11.sp,
                                            color = DevLinkTextSecondary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.OpenInNew,
                                    contentDescription = "Open link",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
