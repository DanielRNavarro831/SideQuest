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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
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

private val PlainDigits = SpanStyle(fontFamily = FontFamily.Serif)

/**
 * IM FELL English only has old-style numerals (its 1 looks like a capital I), so digits are
 * drawn in a plain serif while the surrounding letters keep the old-timey font.
 */
fun String.withPlainDigits(): AnnotatedString = buildAnnotatedString {
    val s = this@withPlainDigits
    append(s)
    var i = 0
    while (i < s.length) {
        if (s[i].isDigit()) {
            val start = i
            while (i < s.length && s[i].isDigit()) i++
            addStyle(PlainDigits, start, i)
        } else {
            i++
        }
    }
}

/** For text fields that only ever hold numbers. */
val NumberFieldStyle: TextStyle
    @Composable get() = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif)

/**
 * Text that shrinks (down to [minSize]) instead of trailing off with "…", so long names always
 * show in full. It uses up to [maxLines] lines at the largest size that fits; only if even
 * [minSize] can't fit does it fall back to an ellipsis.
 */
@Composable
fun FitText(
    text: AnnotatedString,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    maxLines: Int = 1,
    minSize: TextUnit = 11.sp,
) {
    val max = if (style.fontSize.isSpecified) style.fontSize else 16.sp
    BasicText(
        text = text,
        modifier = modifier,
        // Line height follows the shrunken font so wrapped lines don't keep big gaps.
        style = style.copy(color = if (color.isSpecified) color else style.color, lineHeight = 1.15.em),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        autoSize = TextAutoSize.StepBased(minFontSize = minSize, maxFontSize = max, stepSize = 0.5.sp),
    )
}
