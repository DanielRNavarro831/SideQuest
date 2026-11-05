package com.dnavarro.sidequest

import android.app.Application
import android.util.AtomicFile
import com.dnavarro.sidequest.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File
import java.time.LocalDate

class SideQuestApp : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = Repository(AtomicFile(File(filesDir, "sidequest_state.json")), appScope)
        repository.update { it.rollOverDay(LocalDate.now()) }
    }
}
