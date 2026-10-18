package com.zyproo.devlink.ui.common

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.zyproo.devlink.common.toFullMediaUrl
import com.zyproo.devlink.ui.theme.DevLinkBlue

@Composable
fun UserAvatar(
    model: Any?,
    name: String?,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    backgroundColor: Color = DevLinkBlue,
    textColor: Color = Color.White,
    fontSize: TextUnit = 16.sp,
    fallbackText: String? = null
) {
    val initials = remember(name, fallbackText) {
        val raw = when {
            !name.isNullOrBlank() -> name.trim()
            !fallbackText.isNullOrBlank() -> fallbackText.trim()
            else -> "DL"
        }
        val parts = raw.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (parts.size >= 2) {
            "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        } else if (raw.isNotEmpty()) {
            raw.take(2).uppercase()
        } else {
            "DL"
        }
    }

    val imageModel: Any? = when (model) {
        is Uri -> model
        is String -> model.toFullMediaUrl()
        else -> model
    }

    val isModelBlank = imageModel == null || (imageModel is String && imageModel.isBlank())

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (!isModelBlank) {
            SubcomposeAsyncImage(
                model = imageModel,
                contentDescription = name ?: "User Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        )
                    }
                },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        )
                    }
                }
            )
        } else {
            Text(
                text = initials,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize
            )
        }
    }
}
