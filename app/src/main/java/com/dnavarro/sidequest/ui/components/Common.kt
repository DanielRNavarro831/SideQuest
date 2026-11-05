package com.dnavarro.sidequest.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun parchmentFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Parchment.Wood,
    unfocusedBorderColor = Parchment.PaperEdge,
    errorBorderColor = Parchment.Crimson,
    cursorColor = Parchment.Wood,
    focusedTextColor = Parchment.Ink,
    unfocusedTextColor = Parchment.Ink,
    focusedContainerColor = Parchment.Paper.copy(alpha = 0.7f),
    unfocusedContainerColor = Parchment.Paper.copy(alpha = 0.5f),
    errorContainerColor = Parchment.Paper.copy(alpha = 0.7f),
    focusedLabelColor = Parchment.Wood,
    unfocusedLabelColor = Parchment.InkFaded,
)

@Composable
fun ScreenTopBar(heading: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = "Back", tint = Parchment.Ink)
        }
        Text(heading, style = MaterialTheme.typography.titleLarge, color = Parchment.Ink)
    }
}

/** A thin divider with a little flourish in the middle. */
@Composable
fun Ornament(modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).height(1.dp).background(Parchment.PaperEdge))
        Text("  ✦  ", color = Parchment.Gold)
        Box(Modifier.weight(1f).height(1.dp).background(Parchment.PaperEdge))
    }
}

@Composable
fun XpBar(fraction: Float, modifier: Modifier = Modifier, color: Color = Parchment.XpGreen) {
    val animated by animateFloatAsState(fraction.coerceIn(0f, 1f), label = "xpBar")
    val shape = RoundedCornerShape(5.dp)
    Box(
        modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(shape)
            .background(Parchment.Ink.copy(alpha = 0.10f))
            .border(1.dp, Parchment.PaperEdge, shape),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.7f), color))),
        )
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = Parchment.InkFaded,
        modifier = modifier.padding(top = 14.dp, bottom = 4.dp),
    )
}

fun Int.withCommas(): String = "%,d".format(this)
