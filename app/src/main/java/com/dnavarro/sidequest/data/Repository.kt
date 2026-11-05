package com.dnavarro.sidequest.data

import android.util.AtomicFile
import android.util.Log
import com.dnavarro.sidequest.model.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Holds the app's state in memory and saves it to a JSON file on the phone. Writes are
 * batched (at most one per [SAVE_INTERVAL_MS]) so a burst of step events doesn't hammer storage.
 */
class Repository(private val file: AtomicFile, scope: CoroutineScope) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private val saveRequests = Channel<Unit>(Channel.CONFLATED)

    init {
        scope.launch(Dispatchers.IO) {
            for (request in saveRequests) {
                write(_state.value)
                delay(SAVE_INTERVAL_MS)
            }
        }
    }

    fun update(transform: (AppState) -> AppState) {
        var changed = false
        _state.update { old -> transform(old).also { changed = it != old } }
        if (changed) saveRequests.trySend(Unit)
    }

    private fun load(): AppState {
        val base = file.baseFile
        if (!base.exists()) return AppState()
        return try {
            json.decodeFromString(AppState.serializer(), file.readFully().decodeToString()).ensureDefaultSkill()
        } catch (e: Exception) {
            // Keep the unreadable file so progress could be recovered by hand.
            Log.e(TAG, "Couldn't read saved state; starting fresh", e)
            base.copyTo(File(base.parentFile, "${base.name}.corrupt-${System.currentTimeMillis()}"), overwrite = true)
            AppState()
        }
    }

    private fun write(state: AppState) {
        val out = try {
            file.startWrite()
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't open save file", e)
            return
        }
        try {
            out.write(json.encodeToString(AppState.serializer(), state).encodeToByteArray())
            file.finishWrite(out)
        } catch (e: IOException) {
            Log.e(TAG, "Couldn't save state", e)
            file.failWrite(out)
        }
    }

    private companion object {
        const val TAG = "SideQuestRepo"
        const val SAVE_INTERVAL_MS = 1_000L
    }
}
