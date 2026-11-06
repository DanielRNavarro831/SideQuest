package com.dnavarro.sidequest.ui.components

import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnavarro.sidequest.model.Celebration
import com.dnavarro.sidequest.ui.theme.Parchment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private val CoinGold = Color(0xFFF2C14E)
private val CoinShade = Color(0xFFB8860B)
private val SparkleWhite = Color(0xFFFFF6D8)
private val ConfettiColors = listOf(
    Color(0xFFF2C14E), Color(0xFFE05A47), Color(0xFF4F9DDE), Color(0xFF6DBE55), Color(0xFFB57EDC), Color(0xFFFFF6D8),
)

/** True when the phone's "Remove animations" accessibility setting is on. */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * Plays [celebration] over the whole screen and calls [onDone] when it's finished.
 * [playKey] changes whenever a new celebration starts, so back-to-back ones each replay.
 */
@Composable
fun CelebrationHost(playKey: Int, celebration: Celebration?, onStart: (Celebration) -> Unit = {}, onDone: () -> Unit) {
    if (celebration == null) return
    key(playKey, celebration) {
        val view = LocalView.current
        LaunchedEffect(Unit) {
            onStart(celebration)
            when (celebration) {
                is Celebration.QuestCompleted -> buzz(view, strong = false)
                is Celebration.SkillLevelUp, is Celebration.RenownUp -> buzz(view, strong = true)
                is Celebration.HeroLevelUp -> {
                    buzz(view, strong = true)
                    delay(220)
                    buzz(view, strong = true)
                }
            }
        }
        when (celebration) {
            is Celebration.QuestCompleted -> QuestBurst(celebration, onDone)
            is Celebration.SkillLevelUp -> LevelUpBanner(
                title = "LEVEL UP!",
                name = celebration.skillName,
                prefix = "Lv ",
                from = celebration.from.toLong(),
                to = celebration.to.toLong(),
                big = false,
                onDone = onDone,
            ) { ArtImage(celebration.icon.art, size = 96.dp) }
            is Celebration.RenownUp -> LevelUpBanner(
                title = "RENOWN UP!",
                name = "Your deeds are spreading",
                prefix = "Renown ",
                from = celebration.from,
                to = celebration.to,
                big = false,
                onDone = onDone,
            ) { ButtonIcon("button_complete", com.dnavarro.sidequest.R.drawable.ic_check, null, Parchment.Gold, artSize = 96.dp) }
            is Celebration.HeroLevelUp -> LevelUpBanner(
                title = "LEVEL UP!",
                name = "You reached Level ${celebration.to}!",
                prefix = "Level ",
                from = celebration.from.toLong(),
                to = celebration.to.toLong(),
                big = true,
                onDone = onDone,
            ) { HeroPortrait(celebration) }
        }
    }
}

private fun buzz(view: View, strong: Boolean) {
    val effect = when {
        strong -> HapticFeedbackConstants.LONG_PRESS
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> HapticFeedbackConstants.CONFIRM
        else -> HapticFeedbackConstants.VIRTUAL_KEY
    }
    view.performHapticFeedback(effect)
}

// ---------------------------------------------------------------- small: quest complete

/** Coins pop out, "+XP" floats up and fades. Doesn't block taps. */
@Composable
private fun QuestBurst(c: Celebration.QuestCompleted, onDone: () -> Unit) {
    val reduceMotion = rememberReduceMotion()
    val rise = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        rise.animateTo(1f, tween(if (reduceMotion) 900 else 1400, easing = LinearEasing))
        onDone()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!reduceMotion) {
            ParticleCanvas(
                Modifier.fillMaxSize(),
                remember { coinBurst(Random.Default) },
                originX = 0.5f,
                originY = 0.55f,
                gravity = 900f,
            )
        }
        val p = rise.value
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                translationY = -(if (reduceMotion) 0f else 90f * p) * density
                alpha = when {
                    p < 0.12f -> p / 0.12f
                    p > 0.7f -> (1f - p) / 0.3f
                    else -> 1f
                }
                val pop = if (reduceMotion) 1f else (0.7f + 0.3f * (p / 0.15f).coerceAtMost(1f))
                scaleX = pop
                scaleY = pop
            },
        ) {
            Text(
                "+${c.xp} XP".withPlainDigits(),
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = CoinGold,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(Parchment.WoodDark, Offset(3f, 3f), 0f),
                ),
            )
            Text(
                "Quest complete!",
                style = MaterialTheme.typography.titleMedium,
                color = Parchment.Paper,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Parchment.Wood.copy(alpha = 0.92f))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}

// ---------------------------------------------------------------- medium + big: banners

@Composable
private fun LevelUpBanner(
    title: String,
    name: String,
    prefix: String,
    from: Long,
    to: Long,
    big: Boolean,
    onDone: () -> Unit,
    art: @Composable () -> Unit,
) {
    val reduceMotion = rememberReduceMotion()
    val scope = rememberCoroutineScope()
    val enter = remember { Animatable(0f) }
    val artPop = remember { Animatable(if (reduceMotion) 1f else 0.3f) }
    val count = remember { Animatable(from.toFloat()) }
    var leaving by remember { mutableStateOf(false) }

    val dismiss: () -> Unit = {
        if (!leaving) {
            leaving = true
            scope.launch {
                enter.animateTo(0f, tween(180))
                onDone()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            enter.snapTo(1f)
            count.snapTo(to.toFloat())
        } else {
            launch { enter.animateTo(1f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow)) }
            launch { artPop.animateTo(1f, spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessLow)) }
            delay(350)
            count.animateTo(to.toFloat(), tween(600, easing = FastOutSlowInEasing))
        }
    }
    LaunchedEffect(Unit) {
        delay(if (big) 3600 else 2400)
        dismiss()
    }

    val e = enter.value
    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = e.coerceIn(0f, 1f) }
            .background(Color.Black.copy(alpha = if (big) 0.6f else 0.45f))
            // Tap anywhere to skip.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = dismiss),
        contentAlignment = Alignment.Center,
    ) {
        if (!reduceMotion) {
            if (big) {
                ParticleCanvas(Modifier.fillMaxSize(), remember { confettiBurst(Random.Default) }, 0.5f, 0.42f, gravity = 520f)
            }
            ParticleCanvas(
                Modifier.fillMaxSize(),
                remember { sparkleRain(Random.Default, if (big) 70 else 40, if (big) ConfettiColors else listOf(CoinGold, SparkleWhite)) },
                originX = 0f,
                originY = 0f,
                gravity = 60f,
            )
        }
        val shape = RoundedCornerShape(18.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .graphicsLayer {
                    val s = 0.7f + 0.3f * e
                    scaleX = s
                    scaleY = s
                }
                .widthIn(max = 320.dp)
                .padding(24.dp)
                .shadow(12.dp, shape)
                .clip(shape)
                .background(Parchment.Paper)
                .border(3.dp, Parchment.Gold, shape)
                .padding(horizontal = 24.dp, vertical = 20.dp),
        ) {
            Text(
                title,
                fontSize = if (big) 38.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                color = Parchment.Gold,
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = androidx.compose.ui.graphics.Shadow(Parchment.WoodDark.copy(alpha = 0.5f), Offset(2f, 2f), 0f),
                ),
            )
            Box(Modifier.size(if (big) 170.dp else 150.dp), contentAlignment = Alignment.Center) {
                if (!reduceMotion) Rays(Modifier.fillMaxSize(), big)
                Box(Modifier.graphicsLayer {
                    scaleX = artPop.value
                    scaleY = artPop.value
                }) { art() }
            }
            Text(
                "$prefix${count.value.toLong()}".withPlainDigits(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = Parchment.Wood,
            )
            FitText(
                name.withPlainDigits(),
                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center),
                color = Parchment.Ink,
                maxLines = 2,
                minSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
            Text(
                "Tap to continue",
                style = MaterialTheme.typography.labelSmall,
                color = Parchment.InkFaded,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun HeroPortrait(c: Celebration.HeroLevelUp) {
    val glow by rememberInfiniteTransition(label = "heroGlow").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "glow",
    )
    // A pulsing golden glow behind the framed portrait.
    Box(contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(118.dp)
                .shadow((8 + 14 * glow).dp, RoundedCornerShape(10.dp), ambientColor = CoinGold, spotColor = CoinGold),
        )
        FramedBox("frame_portrait", size = 132.dp, inset = FrameInsets.PORTRAIT) {
            ArtImage(c.character.art, size = 90.dp)
        }
    }
}

/** Slowly turning golden sunburst behind the art. */
@Composable
private fun Rays(modifier: Modifier, big: Boolean) {
    val turn by rememberInfiniteTransition(label = "rays").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "turn",
    )
    Canvas(modifier) {
        val c = center
        val r = size.minDimension / 2f
        val rays = 12
        rotate(turn, c) {
            for (i in 0 until rays) {
                val a0 = (i * 2 * PI / rays).toFloat()
                val a1 = a0 + (PI / rays).toFloat()
                val path = Path().apply {
                    moveTo(c.x, c.y)
                    lineTo(c.x + r * cos(a0), c.y + r * sin(a0))
                    lineTo(c.x + r * cos(a1), c.y + r * sin(a1))
                    close()
                }
                drawPath(path, CoinGold.copy(alpha = if (big) 0.45f else 0.35f))
            }
        }
    }
}

// ---------------------------------------------------------------- particles

/**
 * A particle starts at the origin (or at [x0]/[y0] as a fraction of the canvas when set),
 * moves at (vx, vy) dp/s under gravity, spins, and fades out over [life] seconds.
 */
private class Particle(
    val vx: Float,
    val vy: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
    val sparkle: Boolean,
    val delay: Float,
    val life: Float,
    val x0: Float? = null,
    val y0: Float? = null,
)

private fun coinBurst(rng: Random): List<Particle> = List(34) {
    // Mostly upward fan.
    val angle = (-PI / 2 + (rng.nextFloat() - 0.5f) * PI * 1.1f).toFloat()
    val speed = 260f + rng.nextFloat() * 380f
    val sparkle = it % 4 == 0
    Particle(
        vx = cos(angle) * speed,
        vy = sin(angle) * speed,
        size = if (sparkle) 10f else 7f + rng.nextFloat() * 5f,
        color = if (sparkle) SparkleWhite else if (rng.nextBoolean()) CoinGold else CoinShade,
        spin = rng.nextFloat() * 2f - 1f,
        sparkle = sparkle,
        delay = rng.nextFloat() * 0.08f,
        life = 1.0f + rng.nextFloat() * 0.3f,
    )
}

private fun confettiBurst(rng: Random): List<Particle> = List(90) {
    val angle = (rng.nextFloat() * 2 * PI).toFloat()
    val speed = 200f + rng.nextFloat() * 520f
    Particle(
        vx = cos(angle) * speed,
        vy = sin(angle) * speed - 180f,
        size = 6f + rng.nextFloat() * 7f,
        color = ConfettiColors[rng.nextInt(ConfettiColors.size)],
        spin = rng.nextFloat() * 3f - 1.5f,
        sparkle = false,
        delay = rng.nextFloat() * 0.1f,
        life = 2.2f + rng.nextFloat() * 1.0f,
    )
}

private fun sparkleRain(rng: Random, count: Int, colors: List<Color>): List<Particle> = List(count) {
    Particle(
        vx = (rng.nextFloat() - 0.5f) * 40f,
        vy = 90f + rng.nextFloat() * 160f,
        size = 6f + rng.nextFloat() * 6f,
        color = colors[rng.nextInt(colors.size)],
        spin = rng.nextFloat() * 2f - 1f,
        sparkle = rng.nextInt(3) == 0,
        delay = rng.nextFloat() * 1.4f,
        life = 2.6f,
        x0 = rng.nextFloat(),
        y0 = -0.05f + rng.nextFloat() * 0.15f,
    )
}

@Composable
private fun ParticleCanvas(
    modifier: Modifier,
    particles: List<Particle>,
    originX: Float,
    originY: Float,
    gravity: Float,
) {
    var t by remember { mutableFloatStateOf(0f) }
    val maxLife = remember(particles) { particles.maxOf { it.delay + it.life } }
    LaunchedEffect(particles) {
        var start = -1L
        while (t < maxLife) {
            androidx.compose.runtime.withFrameNanos { now ->
                if (start < 0) start = now
                t = (now - start) / 1_000_000_000f
            }
        }
    }
    Canvas(modifier) {
        val d = density
        for (p in particles) {
            val tt = t - p.delay
            if (tt < 0f || tt > p.life) continue
            val ox = (p.x0 ?: originX) * size.width
            val oy = (p.y0 ?: originY) * size.height
            val x = ox + p.vx * tt * d
            val y = oy + (p.vy * tt + 0.5f * gravity * tt * tt) * d
            val fade = (1f - tt / p.life).coerceIn(0f, 1f)
            val alpha = if (tt < 0.08f) tt / 0.08f else fade
            drawPixel(Offset(x, y), p.size * d, p.color.copy(alpha = alpha.coerceIn(0f, 1f)), p.spin * tt * 360f, p.sparkle)
        }
    }
}

/** Blocky, pixel-art style bits: square coins/confetti, or a little plus-shaped sparkle. */
private fun DrawScope.drawPixel(at: Offset, s: Float, color: Color, degrees: Float, sparkle: Boolean) {
    if (sparkle) {
        val arm = s * 1.4f
        val w = s / 2.5f
        drawRect(color, Offset(at.x - arm / 2, at.y - w / 2), Size(arm, w))
        drawRect(color, Offset(at.x - w / 2, at.y - arm / 2), Size(w, arm))
    } else {
        rotate(degrees, at) {
            drawRect(color, Offset(at.x - s / 2, at.y - s / 2), Size(s, s))
        }
    }
}
