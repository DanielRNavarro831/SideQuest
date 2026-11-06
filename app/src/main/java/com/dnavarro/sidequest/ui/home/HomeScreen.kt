package com.dnavarro.sidequest.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dnavarro.sidequest.R
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.dnavarro.sidequest.model.Quest
import com.dnavarro.sidequest.model.Renown
import com.dnavarro.sidequest.model.SillyQuests
import com.dnavarro.sidequest.model.AppState
import com.dnavarro.sidequest.model.CharacterClass
import com.dnavarro.sidequest.model.OverallLevelSettings
import com.dnavarro.sidequest.model.Skill
import com.dnavarro.sidequest.ui.components.ArtImage
import com.dnavarro.sidequest.ui.components.ButtonIcon
import com.dnavarro.sidequest.ui.components.XpBar
import com.dnavarro.sidequest.ui.components.art
import com.dnavarro.sidequest.ui.components.withCommas
import com.dnavarro.sidequest.ui.components.withPlainDigits
import com.dnavarro.sidequest.ui.theme.Parchment

enum class HomeTab { SKILLS, QUESTS }

@Composable
fun HomeScreen(
    state: AppState,
    tab: HomeTab,
    onTab: (HomeTab) -> Unit,
    onName: (String) -> Unit,
    onTitle: (String) -> Unit,
    onCharacter: (CharacterClass) -> Unit,
    onLevelSettings: (OverallLevelSettings) -> Unit,
    onSound: (Boolean) -> Unit = {},
    onOpenSkill: (String) -> Unit,
    onNewSkill: () -> Unit,
    onOpenQuest: (String) -> Unit,
    onToggleQuest: (Quest) -> Unit,
    onNewQuest: () -> Unit,
    badgePulses: Int = 0,
    badgePulsesPlayed: Int = badgePulses,
    onBadgePulsed: (Int) -> Unit = {},
) {
    // Swiping the list and tapping a tab button both move the same pager.
    val pagerState = rememberPagerState(initialPage = tab.ordinal) { HomeTab.entries.size }
    val scope = rememberCoroutineScope()
    val shown = HomeTab.entries[pagerState.currentPage]
    val latestOnTab by rememberUpdatedState(onTab)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { latestOnTab(HomeTab.entries[it]) }
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Pinned: the profile and tabs never scroll away.
            ProfileHeader(
                profile = state.profile,
                overall = state.overallLevel,
                onName = onName,
                onTitle = onTitle,
                onCharacter = onCharacter,
                onLevelSettings = onLevelSettings,
                onSound = onSound,
                badgePulses = badgePulses,
                badgePulsesPlayed = badgePulsesPlayed,
                onBadgePulsed = onBadgePulsed,
            )
            HomeTabs(shown) { scope.launch { pagerState.animateScrollToPage(it.ordinal) } }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1,
            ) { page ->
                when (HomeTab.entries[page]) {
                    HomeTab.SKILLS -> SkillList(state.sortedSkills, onOpenSkill, Modifier.fillMaxSize())
                    HomeTab.QUESTS -> QuestList(state, onOpenQuest, onToggleQuest, Modifier.fillMaxSize())
                }
            }
        }
        val isQuests = shown == HomeTab.QUESTS
        ExtendedFloatingActionButton(
            onClick = if (isQuests) onNewQuest else onNewSkill,
            containerColor = Parchment.Wood,
            contentColor = Parchment.Paper,
            icon = { ButtonIcon("button_new_skill", R.drawable.ic_add, null, Parchment.Paper, artSize = 36.dp) },
            text = { Text(if (isQuests) "New Quest" else "New Skill") },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }
}

private val ListPadding = PaddingValues(start = 4.dp, end = 4.dp, top = 4.dp, bottom = 88.dp)

@Composable
private fun HomeTabs(selected: HomeTab, onSelect: (HomeTab) -> Unit) {
    // Two chunky buttons: the selected one is filled wood (like the New Skill button),
    // the other is a raised parchment button.
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selected
            val shape = RoundedCornerShape(12.dp)
            Box(
                Modifier
                    .weight(1f)
                    .height(46.dp)
                    .shadow(if (isSelected) 4.dp else 2.dp, shape)
                    .clip(shape)
                    .background(if (isSelected) Parchment.Wood else Parchment.Paper)
                    .border(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) Parchment.Gold else Parchment.PaperEdge,
                        shape,
                    )
                    .selectable(selected = isSelected, onClick = { onSelect(tab) }, role = Role.Tab),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val tint = if (isSelected) Parchment.Paper else Parchment.Wood
                    ButtonIcon(
                        if (tab == HomeTab.SKILLS) "button_skills" else "button_quests",
                        R.drawable.ic_check,
                        null,
                        tint,
                        artSize = 30.dp,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (tab == HomeTab.SKILLS) "Skills" else "Quests",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = tint,
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillList(skills: List<Skill>, onOpenSkill: (String) -> Unit, modifier: Modifier) {
    LazyColumn(modifier.fillMaxWidth(), contentPadding = ListPadding) {
        items(skills, key = { it.id }) { skill ->
            SkillRow(skill, onClick = { onOpenSkill(skill.id) }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun QuestList(
    state: AppState,
    onOpenQuest: (String) -> Unit,
    onToggleQuest: (Quest) -> Unit,
    modifier: Modifier,
) {
    val active = state.activeQuests
    val completed = state.completedQuests
    // A new silly line each time the list empties out, but stable while it stays empty.
    val emptyMessage = rememberSaveable(active.isEmpty()) { SillyQuests.emptyMessages.random() }
    LazyColumn(modifier.fillMaxWidth(), contentPadding = ListPadding) {
        item(key = "renown") { RenownPanel(state.renownXp) }
        if (active.isEmpty()) {
            item(key = "empty") {
                Text(
                    emptyMessage,
                    style = MaterialTheme.typography.titleMedium.copy(fontStyle = FontStyle.Italic),
                    color = Parchment.InkFaded,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.animateItem().fillMaxWidth().padding(vertical = 28.dp, horizontal = 16.dp),
                )
            }
        }
        items(active, key = { it.id }) { quest ->
            QuestRow(quest, onClick = { onOpenQuest(quest.id) }, onToggle = { onToggleQuest(quest) }, Modifier.animateItem())
        }
        if (completed.isNotEmpty()) {
            item(key = "completed-header") {
                Text(
                    "Completed",
                    style = MaterialTheme.typography.labelLarge,
                    color = Parchment.InkFaded,
                    modifier = Modifier.animateItem().padding(start = 16.dp, top = 12.dp, bottom = 2.dp),
                )
            }
        }
        items(completed, key = { it.id }) { quest ->
            QuestRow(quest, onClick = { onOpenQuest(quest.id) }, onToggle = { onToggleQuest(quest) }, Modifier.animateItem())
        }
    }
}

@Composable
private fun RenownPanel(renownXp: Long) {
    val shape = RoundedCornerShape(10.dp)
    val progress = Renown.progress(renownXp)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(Parchment.Paper.copy(alpha = 0.9f))
            .border(1.dp, Parchment.PaperEdge, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Renown",
                style = MaterialTheme.typography.titleSmall,
                color = Parchment.Ink,
                modifier = Modifier.weight(1f),
            )
            Text(
                "${Renown.ranks(renownXp)}".withPlainDigits(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Parchment.Wood,
            )
        }
        Spacer(Modifier.height(4.dp))
        XpBar(progress.toFloat() / Renown.XP_PER_RANK, color = Parchment.Gold)
        Text(
            "$progress / ${Renown.XP_PER_RANK} XP to next Renown".withPlainDigits(),
            style = MaterialTheme.typography.labelSmall,
            color = Parchment.InkFaded,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun QuestRow(quest: Quest, onClick: () -> Unit, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    val done = quest.isCompleted
    // A quest finished a moment ago gets a gold flash and its checkmark stamps in.
    val justDone = remember(quest.completedAt) {
        val at = quest.completedAt
        at != null && System.currentTimeMillis() - at < 1500
    }
    val flash = remember(quest.completedAt) { Animatable(if (justDone) 1f else 0f) }
    val stamp = remember(quest.completedAt) { Animatable(if (justDone) 0.4f else 1f) }
    LaunchedEffect(quest.completedAt) {
        if (justDone) {
            launch { stamp.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium)) }
            flash.animateTo(0f, tween(1100))
        }
    }
    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(if (done) 0.5f else 1f)
            .clip(shape)
            .background(Parchment.PaperShade.copy(alpha = 0.55f))
            .background(Parchment.Gold.copy(alpha = 0.55f * flash.value))
            .border(1.dp, Parchment.PaperEdge, shape)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, top = 6.dp, bottom = 6.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtImage(quest.icon.art, size = 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                quest.name.withPlainDigits(),
                style = MaterialTheme.typography.titleMedium.copy(
                    textDecoration = if (done) TextDecoration.LineThrough else TextDecoration.None,
                ),
                color = Parchment.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (quest.description.isNotEmpty()) {
                Text(
                    quest.description.withPlainDigits(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Parchment.InkFaded,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            "${quest.xp.withCommas()} XP".withPlainDigits(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Parchment.Wood,
            modifier = Modifier.padding(start = 8.dp),
        )
        Checkbox(
            modifier = Modifier.graphicsLayer {
                scaleX = stamp.value
                scaleY = stamp.value
            },
            checked = done,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = Parchment.Wood,
                uncheckedColor = Parchment.Wood,
                checkmarkColor = Parchment.Paper,
            ),
        )
    }
}

@Composable
private fun SkillRow(skill: Skill, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier
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
                    skill.name.withPlainDigits(),
                    style = MaterialTheme.typography.titleMedium,
                    color = Parchment.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Lv ${skill.level}".withPlainDigits(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Parchment.Wood,
                )
            }
            Spacer(Modifier.height(6.dp))
            XpBar(skill.xp.toFloat() / skill.xpThreshold)
            Text(
                if (skill.isStepSkill) {
                    "${skill.xp.withCommas()} / ${skill.xpThreshold.withCommas()} steps · resets at midnight".withPlainDigits()
                } else {
                    "${skill.xp} / ${skill.xpThreshold} XP".withPlainDigits()
                },
                style = MaterialTheme.typography.labelSmall,
                color = Parchment.InkFaded,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
