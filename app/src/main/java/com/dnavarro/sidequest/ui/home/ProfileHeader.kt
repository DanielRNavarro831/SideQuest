package com.dnavarro.sidequest.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.dnavarro.sidequest.ui.components.FrameInsets
import com.dnavarro.sidequest.ui.components.FramedBox
import androidx.compose.ui.graphics.lerp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.LevelMode
import com.dnavarro.sidequest.model.Limits
import com.dnavarro.sidequest.model.OverallLevel
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.Profile
import com.dnavarro.sidequest.model.TextRules
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.ButtonIcon
import com.dnavarro.sidequest.ui.components.EditableText
import com.dnavarro.sidequest.ui.components.NumberFieldStyle
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.components.XpBar
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.parchmentFieldColors
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun ProfileHeader(
    profile: Profile,
    overall: OverallLevel,
    onName: (String) -> Unit,
    onTitle: (String) -> Unit,
    onCharacter: (CharacterClass) -> Unit,
    onLevelSettings: (OverallLevelSettings) -> Unit,
    badgePulses: Int = 0,
    onBadgePulsed: (Int) -> Unit = {},
    badgePulsesPlayed: Int = badgePulses,
) {
    var pickingCharacter by rememberSaveable { mutableStateOf(false) }
    var editingLevel by rememberSaveable { mutableStateOf(false) }

    // Light side padding: the scroll already keeps content clear of the torn paper edges.
    Column(Modifier.fillMaxWidth().padding(start = 6.dp, end = 6.dp, top = 14.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FramedBox(
                "frame_portrait",
                size = 88.dp,
                inset = FrameInsets.PORTRAIT,
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { pickingCharacter = true },
            ) { ArtImage(profile.character.art, size = 60.dp) }
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                EditableText(
                    value = profile.name,
                    onSave = onName,
                    maxLength = Limits.NAME_MAX,
                    required = true,
                    placeholder = "Your name",
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Parchment.Ink,
                    ),
                )
                EditableText(
                    value = profile.title,
                    onSave = onTitle,
                    maxLength = Limits.TITLE_MAX,
                    required = false,
                    placeholder = "Add a title…",
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        fontStyle = FontStyle.Italic,
                        color = Parchment.InkFaded,
                    ),
                )
            }
            Spacer(Modifier.width(8.dp))
            LevelBadge(overall.level, badgePulses, badgePulsesPlayed, onBadgePulsed, onClick = { editingLevel = !editingLevel })
        }

        val progress = overall.progress
        val goal = overall.progressGoal
        if (profile.levelSettings.mode == LevelMode.EVERY_X && progress != null && goal != null) {
            // A light panel keeps the bar and its caption readable over the parchment's shading.
            val panel = RoundedCornerShape(10.dp)
            Column(
                Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(panel)
                    .background(Parchment.Paper.copy(alpha = 0.9f))
                    .border(1.dp, Parchment.PaperEdge, panel)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
            ) {
                XpBar(progress.toFloat() / goal, color = Parchment.Gold)
                Text(
                    "$progress / $goal level-ups to next level".withPlainDigits(),
                    style = MaterialTheme.typography.labelMedium,
                    color = Parchment.Ink,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        AnimatedVisibility(visible = editingLevel) {
            LevelModeEditor(
                current = profile.levelSettings,
                onSave = {
                    onLevelSettings(it)
                    editingLevel = false
                },
                onCancel = { editingLevel = false },
            )
        }
    }

    if (pickingCharacter) {
        CharacterPickerDialog(
            current = profile.character,
            onPick = {
                onCharacter(it)
                pickingCharacter = false
            },
            onDismiss = { pickingCharacter = false },
        )
    }
}

@Composable
private fun Portrait(character: CharacterClass, size: Dp, modifier: Modifier = Modifier, selected: Boolean = true) {
    val shape = RoundedCornerShape(12.dp)
    ArtImage(
        character.art,
        size = size,
        modifier = Modifier
            .clip(shape)
            .background(Parchment.PaperShade)
            .border(if (selected) 2.dp else 1.dp, if (selected) Parchment.Gold else Parchment.PaperEdge, shape)
            .then(modifier),
    )
}

@Composable
private fun LevelBadge(level: Int, pulses: Int, played: Int, onPulsed: (Int) -> Unit, onClick: () -> Unit) {
    // After a hero level-up banner, throb a few times the next time the badge is on screen.
    val pulse = remember { Animatable(0f) }
    LaunchedEffect(pulses) {
        if (pulses > played) {
            onPulsed(pulses)
            repeat(3) {
                pulse.animateTo(1f, tween(220))
                pulse.animateTo(0f, tween(260))
            }
        }
    }
    FramedBox(
        "frame_level",
        size = 80.dp,
        inset = FrameInsets.LEVEL,
        // A warm glow fills the opening while it pulses.
        fill = lerp(Parchment.PaperShade, Parchment.Paper, pulse.value),
        modifier = Modifier
            .graphicsLayer {
                val s = 1f + 0.18f * pulse.value
                scaleX = s
                scaleY = s
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        // Tight line heights so the label and number fit the frame's opening together.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "LEVEL",
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 12.sp),
                color = Parchment.InkFaded,
                letterSpacing = 1.sp,
            )
            Text(
                "$level".withPlainDigits(),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, lineHeight = 30.sp),
                color = lerp(Parchment.Wood, Parchment.Gold, pulse.value),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun LevelModeEditor(
    current: OverallLevelSettings,
    onSave: (OverallLevelSettings) -> Unit,
    onCancel: () -> Unit,
) {
    var mode by rememberSaveable { mutableStateOf(current.mode) }
    var xText by rememberSaveable { mutableStateOf(current.x.toString()) }
    var xError by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(onBack = onCancel)

    val save: () -> Unit = {
        val x =if (mode == LevelMode.EVERY_X) xText.toIntOrNull() else current.x
        if (mode == LevelMode.EVERY_X && (x == null || x < Limits.X_MIN)) {
            xError = "* X must be higher than 1"
        } else {
            onSave(OverallLevelSettings(mode, x ?: current.x))
        }
    }

    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clip(shape)
            .background(Parchment.PaperShade.copy(alpha = 0.6f))
            .border(1.dp, Parchment.PaperEdge, shape)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "How is your level calculated?",
                style = MaterialTheme.typography.titleSmall,
                color = Parchment.Ink,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            IconButton(onClick = onCancel) {
                ButtonIcon("button_cancel", R.drawable.ic_close, "Cancel", Parchment.InkFaded, artSize = 28.dp)
            }
            IconButton(onClick = save) {
                ButtonIcon("button_edit", R.drawable.ic_check, "Save", Parchment.Wood, artSize = 28.dp)
            }
        }
        LevelModeOption("Average", "The average of your skill levels, with Renown added as level-ups", mode == LevelMode.AVERAGE) {
            mode = LevelMode.AVERAGE
            xError = null
        }
        LevelModeOption("Sum", "All your skill levels plus your Renown", mode == LevelMode.SUM) {
            mode = LevelMode.SUM
            xError = null
        }
        LevelModeOption("Every X Skill Level Ups", "Gain a level every X skill level-ups (Renown counts too)", mode == LevelMode.EVERY_X) {
            mode = LevelMode.EVERY_X
        }
        if (mode == LevelMode.EVERY_X) {
            OutlinedTextField(
                value = xText,
                onValueChange = {
                    xText = TextRules.filterDigits(it)
                    xError = null
                },
                label = { Text("X") },
                singleLine = true,
                textStyle = NumberFieldStyle,
                isError = xError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                colors = parchmentFieldColors(),
                modifier = Modifier.padding(start = 48.dp).width(120.dp),
            )
            xError?.let {
                Text(
                    it.withPlainDigits(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp),
                )
            }
            Spacer(Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun LevelModeOption(title: String, subtitle: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = Parchment.Wood, unselectedColor = Parchment.InkFaded),
            modifier = Modifier.padding(12.dp),
        )
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Parchment.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Parchment.InkFaded)
        }
    }
}

@Composable
private fun CharacterPickerDialog(
    current: CharacterClass,
    onPick: (CharacterClass) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Parchment.Paper,
        title = { Text("Choose your hero", color = Parchment.Ink) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CharacterClass.entries.chunked(3).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { character ->
                            Column(
                                Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onPick(character) }
                                    .padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Portrait(character, size = 64.dp, selected = character == current)
                                Text(
                                    character.art.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Parchment.Ink,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                        repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close", color = Parchment.Wood) }
        },
    )
}
