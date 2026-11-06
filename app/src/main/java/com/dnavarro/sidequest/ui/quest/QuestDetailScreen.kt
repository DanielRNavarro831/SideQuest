package com.dnavarro.sidequest.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.Quest
import com.dnavarro.sidequest.model.QuestDraft
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.ButtonIcon
import com.dnavarro.sidequest.ui.components.ScreenTopBar
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.withCommas
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun QuestDetailScreen(
    quest: Quest,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    onUncomplete: () -> Unit,
    onSaveEdit: (QuestDraft) -> Unit,
    onDelete: () -> Unit,
) {
    var editing by rememberSaveable(quest.id) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(quest.id) { mutableStateOf(false) }

    if (editing) {
        QuestEditorScreen(
            heading = "Edit Quest",
            initial = QuestDraft.from(quest),
            xpLocked = quest.isCompleted,
            onSave = {
                onSaveEdit(it)
                editing = false
            },
            onCancel = { editing = false },
        )
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenTopBar("Quest", onBack)
        val fade = if (quest.isCompleted) 0.5f else 1f
        ArtImage(quest.icon.art, size = 96.dp, modifier = Modifier.alpha(fade))
        Text(
            quest.name.withPlainDigits(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Parchment.Ink,
            textAlign = TextAlign.Center,
        )
        Text(
            "Reward: ${quest.xp.withCommas()} XP".withPlainDigits(),
            style = MaterialTheme.typography.titleLarge,
            color = Parchment.Wood,
        )
        if (quest.isCompleted) {
            Text("Completed", style = MaterialTheme.typography.titleMedium, color = Parchment.XpGreen)
        }

        Spacer(Modifier.height(16.dp))
        val shape = RoundedCornerShape(10.dp)
        Text(
            quest.description.ifEmpty { "No description yet." }.withPlainDigits(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = if (quest.description.isEmpty()) FontStyle.Italic else FontStyle.Normal,
            ),
            color = if (quest.description.isEmpty()) Parchment.InkFaded else Parchment.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Parchment.PaperShade.copy(alpha = 0.5f))
                .border(1.dp, Parchment.PaperEdge, shape)
                .padding(14.dp),
        )
        Spacer(Modifier.height(20.dp))

        if (quest.isCompleted) {
            OutlinedButton(onClick = onUncomplete) {
                ButtonIcon("button_cancel", R.drawable.ic_close, null, Parchment.Wood)
                Spacer(Modifier.width(6.dp))
                Text("Mark as not done", color = Parchment.Wood)
            }
            Text(
                "Takes back the ${quest.xp.withCommas()} XP it earned.".withPlainDigits(),
                style = MaterialTheme.typography.bodySmall,
                color = Parchment.InkFaded,
                modifier = Modifier.padding(top = 4.dp),
            )
        } else {
            Button(
                onClick = onComplete,
                colors = ButtonDefaults.buttonColors(containerColor = Parchment.Wood, contentColor = Parchment.Paper),
            ) {
                ButtonIcon("button_complete", R.drawable.ic_check, null, Parchment.Paper)
                Spacer(Modifier.width(6.dp))
                Text("Complete Quest")
            }
        }
        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { editing = true }) {
                ButtonIcon("button_edit", R.drawable.ic_edit, null, Parchment.Wood)
                Spacer(Modifier.width(6.dp))
                Text("Edit", color = Parchment.Wood)
            }
            OutlinedButton(onClick = { confirmDelete = true }) {
                ButtonIcon("button_delete", R.drawable.ic_delete, null, Parchment.Crimson)
                Spacer(Modifier.width(6.dp))
                Text("Delete", color = Parchment.Crimson)
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Parchment.Paper,
            title = { Text("Delete ${quest.name}?".withPlainDigits(), color = Parchment.Ink) },
            text = {
                Text(
                    if (quest.isCompleted) "Renown you earned from it is kept." else "This removes the quest. It can't be undone.",
                    color = Parchment.Ink,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("Delete", color = Parchment.Crimson) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Keep it", color = Parchment.Wood) }
            },
        )
    }
}
