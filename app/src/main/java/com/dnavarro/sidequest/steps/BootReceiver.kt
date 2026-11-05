package com.dnavarro.sidequest.steps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Restarts step counting after the phone reboots or the app is updated. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED -> StepCounterService.start(context)
        }
    }
}
