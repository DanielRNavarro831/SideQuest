package com.dnavarro.sidequest.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.ui.theme.Parchment
import kotlin.math.roundToInt

// Geometry of the art in res/drawable-nodpi (source pixels), cut from art-source/scroll/scroll_full.jpg.
private const val TOP_W = 830f
private const val TOP_H = 146f
private const val BOTTOM_W = 810f
private const val BOTTOM_H = 150f
private const val PAPER_LEFT = 52f // where the paper starts under the top roll
private const val PAPER_W = 730f
private const val TUCK = 0.12f // fraction of each roll's height the paper slides underneath
private const val TEXT_MARGIN = 0.05f // keeps content clear of the torn paper edges

/**
 * The app background: a scroll whose rolls stay pinned to the top and bottom of the screen,
 * with the parchment repeating between them. On launch the bottom roll unrolls downward.
 */
@Composable
fun ScrollParchment(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val unroll = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        unroll.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    val topRoll = ImageBitmap.imageResource(R.drawable.scroll_top)
    val bottomRoll = ImageBitmap.imageResource(R.drawable.scroll_bottom)
    val paper = ImageBitmap.imageResource(R.drawable.scroll_paper)

    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .background(Parchment.Backdrop)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        val rollW = maxWidth
        val topH = rollW * (TOP_H / TOP_W)
        val bottomW = rollW * (BOTTOM_W / TOP_W)
        val bottomH = bottomW * (BOTTOM_H / BOTTOM_W)
        val paperX = rollW * (PAPER_LEFT / TOP_W)
        val paperW = rollW * (PAPER_W / TOP_W)
        val tuckTop = topH * TUCK
        val tuckBottom = bottomH * TUCK
        val sheetMax = maxHeight - topH - bottomH

        // The unroll only changes what's drawn and where the bottom roll sits, so animation
        // frames skip recomposition and layout (unroll.value is read in draw/placement lambdas).

        // Paper, tiled downward and tucked under both rolls.
        Box(
            Modifier
                .offset(x = paperX, y = topH - tuckTop)
                .width(paperW)
                .height(sheetMax + tuckTop + tuckBottom)
                .drawBehind {
                    val revealed = tuckTop.toPx() + sheetMax.toPx() * unroll.value + tuckBottom.toPx()
                    val step = (size.width * paper.height / paper.width).roundToInt().coerceAtLeast(1)
                    val dstSize = IntSize(size.width.roundToInt(), step)
                    clipRect(bottom = revealed) {
                        var y = 0
                        while (y < revealed) {
                            drawImage(
                                image = paper,
                                srcOffset = IntOffset.Zero,
                                srcSize = IntSize(paper.width, paper.height),
                                dstOffset = IntOffset(0, y),
                                dstSize = dstSize,
                                filterQuality = FilterQuality.None,
                            )
                            y += step
                        }
                    }
                },
        )

        // Content, laid out at full size and revealed as the scroll unrolls.
        Box(
            Modifier
                .offset(x = paperX, y = topH)
                .width(paperW)
                .height(sheetMax)
                .drawWithContent {
                    clipRect(bottom = size.height * unroll.value) { this@drawWithContent.drawContent() }
                }
                .padding(horizontal = paperW * TEXT_MARGIN),
        ) { content() }

        // Rolls on top.
        Image(
            bitmap = topRoll,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None,
            modifier = Modifier.width(rollW).height(topH),
        )
        Image(
            bitmap = bottomRoll,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .offset { IntOffset(((rollW - bottomW) / 2).roundToPx(), (topH + sheetMax * unroll.value).roundToPx()) }
                .width(bottomW)
                .height(bottomH),
        )
    }
}
