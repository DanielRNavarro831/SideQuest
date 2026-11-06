package com.dnavarro.sidequest.ui.skill

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.Skill
import com.dnavarro.sidequest.model.SkillDraft
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.ButtonIcon
import com.dnavarro.sidequest.ui.components.ScreenTopBar
import com.dnavarro.sidequest.ui.components.XpBar
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.withCommas
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.theme.Parchment
import kotlinx.coroutines.delay

data class StepStatus(
    val hasSensor: Boolean,
    val hasPermission: Boolean,
    val notificationShown: Boolean,
    val onRequestPermission: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onHideNotification: () -> Unit,
)

@Composable
fun SkillDetailScreen(
    skill: Skill,
    stepStatus: StepStatus,
    onBack: () -> Unit,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onSaveEdit: (SkillDraft) -> Unit,
    onDelete: () -> Unit,
) {
    var editing by rememberSaveable(skill.id) { mutableStateOf(false) }
    var confirmDelete by rememberSaveable(skill.id) { mutableStateOf(false) }

    if (editing) {
        SkillEditorScreen(
            heading = "Edit Skill",
            initial = SkillDraft.from(skill),
            showThresholdHint = true,
            onSave = {
                onSaveEdit(it)
                editing = false
            },
            onCancel = { editing = false },
        )
        return
    }

    // Brief "Level up!" flourish whenever the level goes up while you're watching.
    var shownLevel by remember(skill.id) { mutableIntStateOf(skill.level) }
    var celebrate by remember { mutableStateOf(false) }
    LaunchedEffect(skill.level) {
        if (skill.level > shownLevel) {
            shownLevel = skill.level
            celebrate = true
            delay(1600)
            celebrate = false
        } else {
            shownLevel = skill.level
            celebrate = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ScreenTopBar("Skill", onBack)
        ArtImage(skill.icon.art, size = 96.dp)
        Text(
            skill.name.withPlainDigits(),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Parchment.Ink,
            textAlign = TextAlign.Center,
        )
        Text("Level ${skill.level}".withPlainDigits(), style = MaterialTheme.typography.titleLarge, color = Parchment.Wood)
        val flourish by animateFloatAsState(if (celebrate) 1f else 0f, label = "levelUp")
        Box(Modifier.height(36.dp), contentAlignment = Alignment.Center) {
            Text(
                "LEVEL UP!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = Parchment.Gold,
                modifier = Modifier.graphicsLayer {
                    alpha = flourish
                    scaleX = 0.6f + 0.4f * flourish
                    scaleY = 0.6f + 0.4f * flourish
                },
            )
        }
        XpBar(skill.xp.toFloat() / skill.xpThreshold)
        Text(
            if (skill.isStepSkill) {
                "${skill.xp.withCommas()} / ${skill.xpThreshold.withCommas()} steps to next level".withPlainDigits()
            } else {
                "${skill.xp} / ${skill.xpThreshold} XP".withPlainDigits()
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Parchment.InkFaded,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(16.dp))
        val shape = RoundedCornerShape(10.dp)
        Text(
            skill.description.ifEmpty { "No description yet." }.withPlainDigits(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontStyle = if (skill.description.isEmpty()) FontStyle.Italic else FontStyle.Normal,
            ),
            color = if (skill.description.isEmpty()) Parchment.InkFaded else Parchment.Ink,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Parchment.PaperShade.copy(alpha = 0.5f))
                .border(1.dp, Parchment.PaperEdge, shape)
                .padding(14.dp),
        )
        Spacer(Modifier.height(20.dp))

        if (skill.isStepSkill) {
            StepPanel(stepStatus)
            Spacer(Modifier.height(12.dp))
            Text(
                "Daily Adventure can't be edited or deleted.",
                style = MaterialTheme.typography.bodySmall,
                color = Parchment.InkFaded,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                XpButton(R.drawable.ic_remove, "Remove 1 XP", enabled = skill.level > 1 || skill.xp > 0, onClick = onMinus)
                XpButton(R.drawable.ic_add, "Add 1 XP", enabled = true, onClick = onPlus)
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
        }
        Spacer(Modifier.height(24.dp))
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Parchment.Paper,
            title = { Text("Delete ${skill.name}?".withPlainDigits(), color = Parchment.Ink) },
            text = { Text("This removes the skill and all of its progress. It can't be undone.", color = Parchment.Ink) },
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

@Composable
private fun XpButton(icon: Int, description: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Parchment.Wood,
            contentColor = Parchment.Paper,
            // Disabled (nothing to remove) still shows the circle, just faded.
            disabledContainerColor = Parchment.Wood.copy(alpha = 0.35f),
            disabledContentColor = Parchment.Paper.copy(alpha = 0.8f),
        ),
        modifier = Modifier.size(72.dp),
    ) {
        Icon(painterResource(icon), contentDescription = description, modifier = Modifier.size(34.dp))
    }
}

@Composable
private fun StepPanel(status: StepStatus) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        when {
            !status.hasSensor -> Text(
                "This phone doesn't have a step counter, so Daily Adventure can't count steps.",
                style = MaterialTheme.typography.bodyMedium,
                color = Parchment.Crimson,
                textAlign = TextAlign.Center,
            )
            !status.hasPermission -> {
                Text(
                    "SideQuest needs the Physical activity permission to count your steps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Parchment.Ink,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = status.onRequestPermission,
                    colors = ButtonDefaults.buttonColors(containerColor = Parchment.Wood, contentColor = Parchment.Paper),
                ) { Text("Enable step counting") }
                TextButton(onClick = status.onOpenSettings) {
                    Text("Open app settings", color = Parchment.Wood)
                }
            }
            else -> {
                Text(
                    "Counting your steps: 1 step = 1 XP. Progress toward the next level resets at midnight; your level stays.".withPlainDigits(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Parchment.InkFaded,
                    textAlign = TextAlign.Center,
                )
                if (status.notificationShown) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = status.onHideNotification) {
                        Text("Hide the step-counting notification", color = Parchment.Wood)
                    }
                    Text(
                        "Turn it off on the next screen. Steps keep counting.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Parchment.InkFaded,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
