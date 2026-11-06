package com.dnavarro.sidequest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.model.AppState
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.Skill
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.Ornament
import com.dnavarro.sidequest.ui.components.XpBar
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.withCommas
import com.dnavarro.sidequest.ui.theme.Parchment

@Composable
fun HomeScreen(
    state: AppState,
    onName: (String) -> Unit,
    onTitle: (String) -> Unit,
    onCharacter: (CharacterClass) -> Unit,
    onLevelSettings: (OverallLevelSettings) -> Unit,
    onOpenSkill: (String) -> Unit,
    onNewSkill: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Pinned: the profile never scrolls away.
            ProfileHeader(
                profile = state.profile,
                overall = state.overallLevel,
                onName = onName,
                onTitle = onTitle,
                onCharacter = onCharacter,
                onLevelSettings = onLevelSettings,
            )
            Ornament()
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 88.dp),
            ) {
                items(state.skills, key = { it.id }) { skill ->
                    SkillRow(skill, onClick = { onOpenSkill(skill.id) })
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onNewSkill,
            containerColor = Parchment.Wood,
            contentColor = Parchment.Paper,
            icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
            text = { Text("New Skill") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

@Composable
private fun SkillRow(skill: Skill, onClick: () -> Unit) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(Parchment.PaperShade.copy(alpha = 0.55f))
            .border(1.dp, Parchment.PaperEdge, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtImage(skill.icon.art, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    skill.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Parchment.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Lv ${skill.level}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Parchment.Wood,
                )
            }
            Spacer(Modifier.height(6.dp))
            XpBar(skill.xp.toFloat() / skill.xpThreshold)
            Text(
                if (skill.isStepSkill) {
                    "${skill.xp.withCommas()} / ${skill.xpThreshold.withCommas()} steps · resets at midnight"
                } else {
                    "${skill.xp} / ${skill.xpThreshold} XP"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Parchment.InkFaded,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
