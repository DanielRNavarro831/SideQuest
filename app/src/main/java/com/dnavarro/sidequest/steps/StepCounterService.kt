package com.dnavarro.sidequest.steps

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.dnavarro.sidequest.MainActivity
import com.dnavarro.sidequest.R
import com.dnavarro.sidequest.SideQuestApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Listens to the phone's hardware step counter. Android only delivers sensor events to apps
 * in the foreground, so this runs as a low-priority foreground service with a quiet notification.
 * The step counter is a low-power hardware sensor; listening to it barely affects battery.
 */
class StepCounterService : Service(), SensorEventListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var sensorManager: SensorManager? = null
    private val repository get() = (application as SideQuestApp).repository

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!enterForeground()) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (sensorManager == null && !startListening()) {
            stopSelf()
            return START_NOT_STICKY
        }
        running = true
        return START_STICKY
    }

    private fun enterForeground(): Boolean {
        if (!StepPermissions.hasActivityRecognition(this)) return false
        createChannel()
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, buildNotification())
            }
            true
        } catch (e: Exception) {
            Log.w(TAG, "Couldn't start step counting in the foreground", e)
            false
        }
    }

    private fun startListening(): Boolean {
        val manager = getSystemService(SensorManager::class.java) ?: return false
        val sensor = manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) ?: return false
        if (!manager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL, MAX_REPORT_LATENCY_US)) {
            return false
        }
        sensorManager = manager
        // Reset Daily Adventure at midnight even if no steps arrive around then.
        scope.launch {
            while (isActive) {
                repository.update { it.rollOverDay(LocalDate.now()) }
                delay(60_000)
            }
        }
        return true
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val total = event?.values?.firstOrNull()?.toLong() ?: return
        repository.update { it.recordStepCounter(total, bootCount(), LocalDate.now()) }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onDestroy() {
        running = false
        sensorManager?.unregisterListener(this)
        sensorManager = null
        scope.cancel()
        super.onDestroy()
    }

    private fun bootCount(): Int? =
        Settings.Global.getInt(contentResolver, Settings.Global.BOOT_COUNT, -1).takeIf { it >= 0 }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "Step counting", NotificationManager.IMPORTANCE_MIN).apply {
            description = "Keeps Daily Adventure counting your steps."
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openApp = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_walk)
            .setContentTitle("Daily Adventure")
            .setContentText("Counting your steps. You can turn this notice off; counting continues.")
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(openApp)
            .build()
    }

    companion object {
        private const val TAG = "StepCounterService"
        const val CHANNEL_ID = "step_counting"
        private const val NOTIFICATION_ID = 1
        private const val MAX_REPORT_LATENCY_US = 5_000_000

        /** True while this process's service is counting. */
        @Volatile
        private var running = false

        /**
         * Safe to call repeatedly; does nothing without permission or a step sensor. Skips the
         * restart when already counting, because restarting re-posts the notification the
         * user may have swiped away.
         */
        fun start(context: Context) {
            if (running) return
            if (!StepPermissions.hasActivityRecognition(context) || !StepPermissions.hasStepSensor(context)) return
            try {
                ContextCompat.startForegroundService(context, Intent(context, StepCounterService::class.java))
            } catch (e: IllegalStateException) {
                Log.w(TAG, "Not allowed to start step counting right now", e)
            }
        }
    }
}
