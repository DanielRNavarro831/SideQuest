package com.dnavarro.sidequest.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.model.LevelMode
import com.dnavarro.sidequest.model.Limits
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.Profile
import com.dnavarro.sidequest.model.TextRules
import com.dnavarro.sidequest.ui.components.NumberFieldStyle
import com.dnavarro.sidequest.ui.components.ScreenTopBar
import com.dnavarro.sidequest.ui.components.parchmentFieldColors
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.skill.StepStatus
import com.dnavarro.sidequest.ui.theme.Parchment

/** Everything adjustable in one place. Changes apply as soon as they're made. */
@Composable
fun SettingsScreen(
    profile: Profile,
    stepStatus: StepStatus,
    onLevelSettings: (OverallLevelSettings) -> Unit,
    onSound: (Boolean) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(onBack = onBack)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ScreenTopBar("Settings", onBack)

        Section("Hero level", "How your overall level is calculated") {
            LevelModeOptions(profile.levelSettings, onLevelSettings)
        }
        Section("Sound") {
            SwitchRow(
                title = "Sound effects",
                subtitle = "Plays on level-ups and finished quests. Always quiet when your phone is on silent.",
                checked = profile.soundOn,
                onChange = onSound,
            )
        }
        Section("Step counting", "Daily Adventure levels up as you walk") {
            StepSettings(stepStatus)
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(shape)
            .background(Parchment.PaperShade.copy(alpha = 0.6f))
            .border(1.dp, Parchment.PaperEdge, shape)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Parchment.Ink, modifier = Modifier.padding(start = 8.dp))
        subtitle?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = Parchment.InkFaded, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun LevelModeOptions(current: OverallLevelSettings, onChange: (OverallLevelSettings) -> Unit) {
    var xText by rememberSaveable { mutableStateOf(current.x.toString()) }
    val xValue = xText.toIntOrNull()
    val xError = if (xValue == null || xValue < Limits.X_MIN) "* X must be higher than 1" else null

    LevelModeOption("Average", "The average of your skill levels, with Renown added as level-ups", current.mode == LevelMode.AVERAGE) {
        onChange(current.copy(mode = LevelMode.AVERAGE))
    }
    LevelModeOption("Sum", "All your skill levels plus your Renown", current.mode == LevelMode.SUM) {
        onChange(current.copy(mode = LevelMode.SUM))
    }
    LevelModeOption("Every X Skill Level Ups", "Gain a level every X skill level-ups (Renown counts too)", current.mode == LevelMode.EVERY_X) {
        onChange(current.copy(mode = LevelMode.EVERY_X))
    }
    if (current.mode == LevelMode.EVERY_X) {
        OutlinedTextField(
            value = xText,
            onValueChange = {
                xText = TextRules.filterDigits(it)
                // Only valid values are saved; the last good one stays until then.
                val x = xText.toIntOrNull()
                if (x != null && x >= Limits.X_MIN) onChange(current.copy(x = x))
            },
            label = { Text("X") },
            singleLine = true,
            textStyle = NumberFieldStyle,
            isError = xError != null,
            supportingText = xError?.let { { Text(it.withPlainDigits()) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            colors = parchmentFieldColors(),
            modifier = Modifier.padding(start = 48.dp, bottom = 4.dp).width(160.dp),
        )
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
private fun SwitchRow(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onChange, role = Role.Switch)
            .padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = Parchment.Ink)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Parchment.InkFaded)
        }
        Spacer(Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Parchment.Paper,
                checkedTrackColor = Parchment.Wood,
                uncheckedThumbColor = Parchment.WoodLight,
                uncheckedTrackColor = Parchment.PaperShade,
                uncheckedBorderColor = Parchment.PaperEdge,
            ),
        )
    }
}

@Composable
private fun StepSettings(status: StepStatus) {
    val body = MaterialTheme.typography.bodyMedium
    Column(Modifier.padding(horizontal = 8.dp)) {
        when {
            !status.hasSensor -> Text(
                "This phone doesn't have a step counter, so Daily Adventure can't count steps.",
                style = body,
                color = Parchment.Crimson,
            )
            !status.hasPermission -> {
                Text("Step counting is off. SideQuest needs the Physical activity permission to count steps.", style = body, color = Parchment.Ink)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = status.onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Parchment.Wood, contentColor = Parchment.Paper),
                ) { Text("Enable step counting") }
                TextButton(onClick = status.onOpenSettings) { Text("Open app settings", color = Parchment.Wood) }
            }
            else -> {
                Text("Step counting is on.", style = body, color = Parchment.Ink)
                Spacer(Modifier.height(10.dp))
                Text("Step-counting notification", style = MaterialTheme.typography.bodyLarge, color = Parchment.Ink)
                Text(
                    if (status.notificationShown) {
                        "Android shows a notification while steps are being counted. You can hide it; steps keep counting."
                    } else {
                        "Hidden. Steps still count in the background."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Parchment.InkFaded,
                )
                Spacer(Modifier.height(6.dp))
                // Android only lets the user change this on its own notification screen.
                OutlinedButton(onClick = status.onHideNotification) {
                    Text(if (status.notificationShown) "Hide notification" else "Notification settings", color = Parchment.Wood)
                }
            }
        }
    }
}
