package com.dnavarro.sidequest.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.ui.theme.Parchment

/**
 * A square box with decorative frame art (res/drawable-nodpi/[frameArt].png) drawn over its
 * edges. [content] sits in the frame's opening, inset by [inset] of the size on each side, on a
 * [fill] background. Without the art, a plain gold border is drawn instead; [content] is told
 * which one it got so it can add anything the art would otherwise provide.
 */
@SuppressLint("DiscouragedApi") // Looked up by name so art can be added without code changes.
@Composable
fun FramedBox(
    frameArt: String,
    size: Dp,
    inset: Float,
    modifier: Modifier = Modifier,
    fill: Color = Parchment.PaperShade,
    content: @Composable (hasFrame: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val resId = remember(frameArt) { context.resources.getIdentifier(frameArt, "drawable", context.packageName) }
    val hasFrame = resId != 0
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        val shape = RoundedCornerShape(if (hasFrame) 2.dp else 12.dp)
        Box(
            Modifier
                .fillMaxSize()
                .padding(if (hasFrame) size * inset else 0.dp)
                .clip(shape)
                .background(fill)
                .then(if (hasFrame) Modifier else Modifier.border(2.dp, Parchment.Gold, shape)),
            contentAlignment = Alignment.Center,
        ) { content(hasFrame) }
        if (hasFrame) {
            Image(
                bitmap = ImageBitmap.imageResource(resId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
            )
        }
    }
}

/** Fraction of each frame covered by its border, measured from the art. */
object FrameInsets {
    const val PORTRAIT = 0.145f
    const val LEVEL = 0.17f
}
