package com.dnavarro.sidequest.sound

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.dnavarro.sidequest.model.Celebration

/**
 * Short sound effects for celebrations. Each sound is optional: drop res/raw/<name>.ogg in and
 * it's used, otherwise that celebration is silent. Nothing plays while the phone is on silent or
 * vibrate, and starting a new sound stops one still playing so they never pile up.
 */
class SoundEffects(context: Context) {
    private val audio = context.getSystemService(AudioManager::class.java)
    private val pool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    /** Sound name -> SoundPool id, for the files that exist. */
    private val ids: Map<String, Int>
    private var currentStream = 0

    init {
        ids = NAMES.mapNotNull { name ->
            val res = rawId(context, name)
            if (res == 0) null else name to pool.load(context, res, 1)
        }.toMap()
    }

    fun play(celebration: Celebration) {
        if (audio?.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        val id = soundFor(celebration)?.let { ids[it] } ?: return
        if (currentStream != 0) pool.stop(currentStream)
        currentStream = pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()

    private fun soundFor(c: Celebration): String? = when (c) {
        is Celebration.QuestCompleted -> QUEST
        is Celebration.SkillLevelUp -> LEVEL_UP
        // Renown has its own sound, or borrows the level-up one.
        is Celebration.RenownUp -> if (RENOWN in ids) RENOWN else LEVEL_UP
        is Celebration.HeroLevelUp -> HERO
    }

    private companion object {
        const val QUEST = "sfx_quest_complete"
        const val LEVEL_UP = "sfx_level_up"
        const val RENOWN = "sfx_renown_up"
        const val HERO = "sfx_hero_level_up"
        val NAMES = listOf(QUEST, LEVEL_UP, RENOWN, HERO)

        @SuppressLint("DiscouragedApi") // Looked up by name so sounds can be added without code changes.
        fun rawId(context: Context, name: String) =
            context.resources.getIdentifier(name, "raw", context.packageName)
    }
}

/** One [SoundEffects] for the screen's lifetime, released when it leaves. */
@Composable
fun rememberSoundEffects(): SoundEffects {
    val context = LocalContext.current
    val sounds = remember { SoundEffects(context.applicationContext) }
    DisposableEffect(sounds) { onDispose { sounds.release() } }
    return sounds
}
