package com.dnavarro.sidequest

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import com.dnavarro.sidequest.ui.components.CelebrationHost
import com.dnavarro.sidequest.model.Celebration
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dnavarro.sidequest.model.QuestDraft
import com.dnavarro.sidequest.model.SkillDraft
import com.dnavarro.sidequest.steps.StepCounterService
import com.dnavarro.sidequest.steps.StepPermissions
import com.dnavarro.sidequest.ui.components.ScrollParchment
import com.dnavarro.sidequest.ui.home.HomeScreen
import com.dnavarro.sidequest.ui.home.HomeTab
import com.dnavarro.sidequest.ui.quest.QuestDetailScreen
import com.dnavarro.sidequest.ui.quest.QuestEditorScreen
import com.dnavarro.sidequest.ui.skill.SkillDetailScreen
import com.dnavarro.sidequest.ui.skill.SkillEditorScreen
import com.dnavarro.sidequest.ui.skill.StepStatus
import com.dnavarro.sidequest.ui.theme.SideQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            SideQuestTheme { SideQuestRoot() }
        }
    }
}

private const val ROUTE_HOME = "home"
private const val ROUTE_NEW = "new"
private const val SKILL_PREFIX = "skill/"
private const val ROUTE_NEW_QUEST = "new-quest"
private const val QUEST_PREFIX = "quest/"

@Composable
private fun SideQuestRoot(vm: MainViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // ---- Step counting permission ----
    val hasSensor = remember { StepPermissions.hasStepSensor(context) }
    var hasPermission by remember { mutableStateOf(StepPermissions.hasActivityRecognition(context)) }
    var notificationShown by remember { mutableStateOf(StepPermissions.isStepNotificationShown(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.markPermissionPrompted()
        hasPermission = StepPermissions.hasActivityRecognition(context)
        if (hasPermission) StepCounterService.start(context)
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasPermission = StepPermissions.hasActivityRecognition(context)
        notificationShown = StepPermissions.isStepNotificationShown(context)
        vm.onResume()
        if (hasPermission) StepCounterService.start(context)
    }
    LaunchedEffect(Unit) {
        // Ask once on first launch; after that the Daily Adventure screen has a button for it.
        if (hasSensor && !hasPermission && !vm.state.value.steps.permissionPrompted) {
            permissionLauncher.launch(StepPermissions.permissionsToRequest())
        }
    }
    val stepStatus = StepStatus(
        hasSensor = hasSensor,
        hasPermission = hasPermission,
        notificationShown = notificationShown,
        onRequestPermission = { permissionLauncher.launch(StepPermissions.permissionsToRequest()) },
        onHideNotification = { context.startActivity(StepPermissions.stepNotificationSettingsIntent(context)) },
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
            )
        },
    )

    // ---- Navigation ----
    var route by rememberSaveable { mutableStateOf(ROUTE_HOME) }
    BackHandler(enabled = route != ROUTE_HOME) { route = ROUTE_HOME }
    val openSkill = if (route.startsWith(SKILL_PREFIX)) state.skill(route.removePrefix(SKILL_PREFIX)) else null
    val openQuest = if (route.startsWith(QUEST_PREFIX)) state.quest(route.removePrefix(QUEST_PREFIX)) else null
    LaunchedEffect(route, openSkill, openQuest) {
        if (route.startsWith(SKILL_PREFIX) && openSkill == null) route = ROUTE_HOME
        if (route.startsWith(QUEST_PREFIX) && openQuest == null) route = ROUTE_HOME
    }
    var tab by rememberSaveable { mutableStateOf(HomeTab.SKILLS) }

    // ---- Fanfare ----
    val celebrations by vm.celebrations.collectAsStateWithLifecycle()
    var celebrationsPlayed by remember { mutableIntStateOf(0) }
    // Bumped after a hero level-up banner closes, so the level badge pulses once you can see it.
    var badgePulses by remember { mutableIntStateOf(0) }
    var badgePulsesPlayed by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
    ScrollParchment {
        when {
            route == ROUTE_NEW_QUEST -> QuestEditorScreen(
                heading = "New Quest",
                initial = remember { QuestDraft.newQuest() },
                xpLocked = false,
                onSave = {
                    vm.addQuest(it)
                    route = ROUTE_HOME
                },
                onCancel = { route = ROUTE_HOME },
            )
            openQuest != null -> QuestDetailScreen(
                quest = openQuest,
                onBack = { route = ROUTE_HOME },
                onComplete = {
                    // Back to the list so the quest can be seen sliding into the completed section.
                    vm.completeQuest(openQuest.id)
                    route = ROUTE_HOME
                },
                onUncomplete = { vm.uncompleteQuest(openQuest.id) },
                onSaveEdit = { vm.editQuest(openQuest.id, it) },
                onDelete = {
                    vm.deleteQuest(openQuest.id)
                    route = ROUTE_HOME
                },
            )
            route == ROUTE_NEW -> SkillEditorScreen(
                heading = "New Skill",
                initial = remember { SkillDraft.newSkill() },
                showThresholdHint = false,
                onSave = {
                    vm.addSkill(it)
                    route = ROUTE_HOME
                },
                onCancel = { route = ROUTE_HOME },
            )
            openSkill != null -> SkillDetailScreen(
                skill = openSkill,
                stepStatus = stepStatus,
                onBack = { route = ROUTE_HOME },
                onPlus = { vm.gainXp(openSkill.id) },
                onMinus = { vm.loseXp(openSkill.id) },
                onSaveEdit = { vm.editSkill(openSkill.id, it) },
                onDelete = {
                    vm.deleteSkill(openSkill.id)
                    route = ROUTE_HOME
                },
            )
            else -> HomeScreen(
                state = state,
                tab = tab,
                onTab = { tab = it },
                onName = vm::setName,
                onTitle = vm::setTitle,
                onCharacter = vm::setCharacter,
                onLevelSettings = vm::setLevelSettings,
                onOpenSkill = { route = SKILL_PREFIX + it },
                onNewSkill = { route = ROUTE_NEW },
                onOpenQuest = { route = QUEST_PREFIX + it },
                onToggleQuest = { if (it.isCompleted) vm.uncompleteQuest(it.id) else vm.completeQuest(it.id) },
                onNewQuest = { route = ROUTE_NEW_QUEST },
                badgePulses = badgePulses,
                badgePulsesPlayed = badgePulsesPlayed,
                onBadgePulsed = { badgePulsesPlayed = it },
            )
        }
    }
    val current = celebrations.firstOrNull()
    CelebrationHost(
        playKey = celebrationsPlayed,
        celebration = current,
        onDone = {
            if (current is Celebration.HeroLevelUp) badgePulses++
            celebrationsPlayed++
            vm.celebrationDone()
        },
    )
    }
}
