package com.dnavarro.sidequest.model

import kotlinx.serialization.json.Json
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundSettingTest {
    @Test fun `sound is on by default, including for older saves`() {
        assertTrue(AppState().profile.soundOn)
        val old = Json { ignoreUnknownKeys = true }.decodeFromString(AppState.serializer(), """{"profile":{"name":"Hero"}}""")
        assertTrue(old.profile.soundOn)
    }

    @Test fun `sound can be turned off and stays off after saving`() {
        val json = Json { encodeDefaults = true }
        val off = AppState().withSound(false)
        assertFalse(off.profile.soundOn)
        assertFalse(json.decodeFromString(AppState.serializer(), json.encodeToString(AppState.serializer(), off)).profile.soundOn)
    }
}
