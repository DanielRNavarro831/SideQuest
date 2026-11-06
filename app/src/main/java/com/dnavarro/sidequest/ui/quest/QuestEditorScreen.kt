package com.dnavarro.sidequest.ui.quest

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.Limits
import com.dnavarro.sidequest.model.QuestDraft
import com.dnavarro.sidequest.model.Renown
import com.dnavarro.sidequest.model.TextRules
import com.dnavarro.sidequest.ui.components.ButtonIcon
import com.dnavarro.sidequest.ui.components.NumberFieldStyle
import com.dnavarro.sidequest.ui.components.ScreenTopBar
import com.dnavarro.sidequest.ui.components.SectionLabel
import com.dnavarro.sidequest.ui.components.parchmentFieldColors
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.skill.IconPicker
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun QuestEditorScreen(
    heading: String,
    initial: QuestDraft,
    /** A completed quest's XP is already banked, so it can't be changed. */
    xpLocked: Boolean,
    onSave: (QuestDraft) -> Unit,
    onCancel: () -> Unit,
) {
    BackHandler(onBack = onCancel)
    var name by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(initial.name, TextRange(initial.name.length)))
    }
    var description by rememberSaveable { mutableStateOf(initial.description) }
    var xpText by rememberSaveable { mutableStateOf(initial.xp.toString()) }
    var icon by rememberSaveable { mutableStateOf(initial.icon) }
    var nameError by rememberSaveable { mutableStateOf<String?>(null) }
    var xpError by rememberSaveable { mutableStateOf<String?>(null) }

    val save: () -> Unit = {
        val cleanName = TextRules.cleanName(name.text, Limits.QUEST_NAME_MAX)
        val xp = if (xpLocked) initial.xp else xpText.toIntOrNull()
        nameError = if (cleanName.isEmpty()) TextRules.REQUIRED_ERROR else null
        xpError = if (xp == null || xp !in Limits.QUEST_XP_MIN..Limits.QUEST_XP_MAX) {
            "* XP reward must be between ${Limits.QUEST_XP_MIN} and ${Limits.QUEST_XP_MAX}"
        } else {
            null
        }
        if (nameError == null && xpError == null && xp != null) {
            onSave(QuestDraft(cleanName, TextRules.cleanDescription(description), icon, xp))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            ScreenTopBar(heading, onCancel)

            SectionLabel("Name")
            OutlinedTextField(
                value = name,
                onValueChange = { next ->
                    val filtered = TextRules.filterName(next.text, Limits.QUEST_NAME_MAX)
                    name = if (filtered == next.text) next else TextFieldValue(filtered, TextRange(filtered.length))
                    nameError = null
                },
                singleLine = true,
                isError = nameError != null,
                supportingText = {
                    Text((nameError ?: "${name.text.length} / ${Limits.QUEST_NAME_MAX}").withPlainDigits())
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
                            "${description.length} / ${Limits.DESCRIPTION_MAX} — only the first ${Limits.DESCRIPTION_MAX} will be saved".withPlainDigits()
                        } else {
                            "${description.length} / ${Limits.DESCRIPTION_MAX}".withPlainDigits()
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

            SectionLabel("XP reward")
            OutlinedTextField(
                value = xpText,
                textStyle = NumberFieldStyle,
                onValueChange = {
                    xpText = TextRules.filterDigits(it)
                    xpError = null
                },
                enabled = !xpLocked,
                singleLine = true,
                isError = xpError != null,
                supportingText = {
                    val hint = when {
                        xpLocked -> "Already earned, so the reward can't change."
                        else -> renownHint(xpText.toIntOrNull())
                    }
                    Text((xpError ?: hint).withPlainDigits())
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                colors = parchmentFieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )

            SectionLabel("Icon")
            IconPicker(selected = icon, onSelect = { icon = it })
            Spacer(Modifier.height(12.dp))
        }

        Box(Modifier.fillMaxWidth().height(1.dp).background(Parchment.PaperEdge.copy(alpha = 0.6f)))
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
        ) {
            OutlinedButton(onClick = onCancel) {
                ButtonIcon("button_cancel", R.drawable.ic_close, null, Parchment.Wood)
                Spacer(Modifier.width(6.dp))
                Text("Cancel", color = Parchment.Wood)
            }
            Button(
                onClick = save,
                colors = ButtonDefaults.buttonColors(containerColor = Parchment.Wood, contentColor = Parchment.Paper),
            ) {
                ButtonIcon("button_edit", R.drawable.ic_check, null, Parchment.Paper)
                Spacer(Modifier.width(6.dp))
                Text("Save")
            }
        }
    }
}

/** e.g. "≈ 0.5 skill level-ups (100 XP = 1 Renown)". */
private fun renownHint(xp: Int?): String {
    val perRank = Renown.XP_PER_RANK
    val base = "$perRank XP = 1 Renown = 1 skill level-up"
    if (xp == null || xp <= 0) return base
    val levelUps = xp.toDouble() / perRank
    val shown = if (levelUps == Math.floor(levelUps)) "%.0f".format(levelUps) else "%.2f".format(levelUps).trimEnd('0')
    return "≈ $shown level-ups · $base"
}
