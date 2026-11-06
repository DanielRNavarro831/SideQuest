package com.dnavarro.sidequest.ui.skill

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.Limits
import com.dnavarro.sidequest.model.SkillDraft
import com.dnavarro.sidequest.model.SkillIcon
import com.dnavarro.sidequest.model.TextRules
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.ScreenTopBar
import com.dnavarro.sidequest.ui.components.SectionLabel
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.parchmentFieldColors
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun SkillEditorScreen(
    heading: String,
    initial: SkillDraft,
    showThresholdHint: Boolean,
    onSave: (SkillDraft) -> Unit,
    onCancel: () -> Unit,
) {
    BackHandler(onBack = onCancel)
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initial.name, TextRange(initial.name.length)))
    }
    var description by rememberSaveable { mutableStateOf(initial.description) }
    var threshold by rememberSaveable { mutableStateOf(initial.xpThreshold.toString()) }
    var icon by rememberSaveable { mutableStateOf(initial.icon) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var thresholdError by rememberSaveable { mutableStateOf<String?>(null) }

    val save: () -> Unit = {
        val cleanName = TextRules.cleanName(name.text, Limits.SKILL_NAME_MAX)
        val xp = threshold.toIntOrNull()
        nameError = if (cleanName.isEmpty()) TextRules.REQUIRED_ERROR else null
        thresholdError = if (xp == null || xp !in Limits.THRESHOLD_MIN..Limits.THRESHOLD_MAX) {
            "* XP to level up must be between ${Limits.THRESHOLD_MIN} and ${Limits.THRESHOLD_MAX}"
        } else {
            null
        }
        if (nameError == null && thresholdError == null && xp != null) {
            onSave(SkillDraft(cleanName, TextRules.cleanDescription(description), icon, xp))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        ScreenTopBar(heading, onCancel)

        SectionLabel("Name")
        OutlinedTextField(
            value = name,
            onValueChange = { next ->
                val filtered = TextRules.filterName(next.text, Limits.SKILL_NAME_MAX)
                name = if (filtered == next.text) next else TextFieldValue(filtered, TextRange(filtered.length))
                nameError = null
            },
            singleLine = true,
            isError = nameError != null,
            supportingText = {
                Text(nameError ?: "${name.text.length} / ${Limits.SKILL_NAME_MAX}")
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            colors = parchmentFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("Description (optional)")
        val over = description.length > Limits.DESCRIPTION_MAX
        OutlinedTextField(
            value = description,
            onValueChange = { description = TextRules.filterDescription(it) },
            minLines = 2,
            supportingText = {
                Text(
                    if (over) {
                        "${description.length} / ${Limits.DESCRIPTION_MAX} — only the first ${Limits.DESCRIPTION_MAX} will be saved"
                    } else {
                        "${description.length} / ${Limits.DESCRIPTION_MAX}"
                    },
                    color = if (over) Parchment.Crimson else Parchment.InkFaded,
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Next,
            ),
            colors = parchmentFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        SectionLabel("XP to level up")
        OutlinedTextField(
            value = threshold,
            onValueChange = {
                threshold = TextRules.filterDigits(it)
                thresholdError = null
            },
            singleLine = true,
            isError = thresholdError != null,
            supportingText = {
                val hint = if (showThresholdHint) "Changing this recalculates the level from all XP ever earned." else null
                (thresholdError ?: hint)?.let { Text(it) }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            colors = parchmentFieldColors(),
            modifier = Modifier.width(160.dp),
        )

        SectionLabel("Icon")
        IconPicker(selected = icon, onSelect = { icon = it })

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)) {
            OutlinedButton(onClick = onCancel) { Text("Cancel", color = Parchment.Wood) }
            Button(
                onClick = save,
                colors = ButtonDefaults.buttonColors(containerColor = Parchment.Wood, contentColor = Parchment.Paper),
            ) {
                Icon(painterResource(R.drawable.ic_check), contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Save")
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun IconPicker(selected: SkillIcon, onSelect: (SkillIcon) -> Unit) {
    val perRow = 3
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SkillIcon.entries.chunked(perRow).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { option ->
                    val isSelected = option == selected
                    val shape = RoundedCornerShape(10.dp)
                    Column(
                        Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(if (isSelected) Parchment.PaperShade else Color.Transparent)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Parchment.Wood else Parchment.PaperEdge, shape)
                            .clickable { onSelect(option) }
                            .padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ArtImage(option.art, size = 48.dp)
                        Text(
                            option.art.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = Parchment.Ink,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                        )
                    }
                }
                repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
