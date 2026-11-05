package com.dnavarro.sidequest.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.ui.theme.Parchment
import kotlin.random.Random

private val RodHeight = 30.dp
private val KnobWidth = 14.dp
private val SheetInset = 16.dp

/**
 * The app background: a scroll whose rolled ends stay pinned to the top and bottom of the
 * screen. On launch the bottom rod unrolls downward to reveal the parchment.
 */
@Composable
fun ScrollParchment(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val unroll = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        unroll.animateTo(1f, tween(durationMillis = 900, easing = FastOutSlowInEasing))
    }
    Column(
        modifier
            .fillMaxSize()
            .background(Parchment.Backdrop)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    ) {
        ScrollRod()
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val sheetMax = maxHeight - RodHeight
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .padding(horizontal = SheetInset)
                        .fillMaxWidth()
                        .height(sheetMax * unroll.value)
                        .clipToBounds()
                        .drawBehind { drawParchment() },
                ) {
                    // Laid out at full height from the start so content doesn't reflow while unrolling.
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(Alignment.Top, unbounded = true)
                            .height(sheetMax),
                    ) { content() }
                }
                ScrollRod()
            }
        }
    }
}

private fun DrawScope.drawParchment() {
    drawRect(
        Brush.horizontalGradient(
            0f to Parchment.PaperEdge,
            0.05f to Parchment.PaperShade,
            0.18f to Parchment.Paper,
            0.82f to Parchment.Paper,
            0.95f to Parchment.PaperShade,
            1f to Parchment.PaperEdge,
        ),
    )
    // A few faint stains, fixed in place so they're revealed as the scroll unrolls.
    val random = Random(42)
    val stainArea = 1600.dp.toPx()
    repeat(16) {
        val center = Offset(random.nextFloat() * size.width, random.nextFloat() * stainArea)
        val radius = (40 + random.nextFloat() * 90).dp.toPx()
        drawCircle(
            Brush.radialGradient(
                listOf(Parchment.PaperEdge.copy(alpha = 0.16f), Color.Transparent),
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
    val shadow = 18.dp.toPx()
    drawRect(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.18f), Color.Transparent), endY = shadow))
    drawRect(
        Brush.verticalGradient(
            listOf(Color.Transparent, Color.Black.copy(alpha = 0.18f)),
            startY = size.height - shadow,
            endY = size.height,
        ),
    )
}

@Composable
private fun ScrollRod() {
    Canvas(Modifier.fillMaxWidth().height(RodHeight)) {
        val knob = KnobWidth.toPx()
        val h = size.height
        // The rolled-up paper.
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Parchment.PaperEdge, Parchment.Paper, Parchment.PaperShade, Parchment.PaperEdge),
            ),
            topLeft = Offset(knob, 0f),
            size = Size(size.width - 2 * knob, h),
            cornerRadius = CornerRadius(4.dp.toPx()),
        )
        drawLine(
            Parchment.PaperEdge,
            Offset(knob, h * 0.32f),
            Offset(size.width - knob, h * 0.32f),
            strokeWidth = 1.dp.toPx(),
        )
        // Wooden handles at each end.
        val handleTop = h * 0.14f
        val handleHeight = h * 0.72f
        listOf(0f, size.width - knob).forEach { x ->
            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(Parchment.WoodDark, Parchment.WoodLight, Parchment.Wood, Parchment.WoodDark),
                    startY = handleTop,
                    endY = handleTop + handleHeight,
                ),
                topLeft = Offset(x, handleTop),
                size = Size(knob, handleHeight),
                cornerRadius = CornerRadius(knob / 2),
            )
        }
    }
}
