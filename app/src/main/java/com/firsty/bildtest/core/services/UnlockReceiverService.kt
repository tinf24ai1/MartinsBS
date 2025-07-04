package com.firsty.bildtest.core.services

import android.Manifest
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import android.os.IBinder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

import com.firsty.bildtest.MainActivity
import com.firsty.bildtest.R
import com.firsty.bildtest.core.util.Strings
import me.leolin.shortcutbadger.ShortcutBadger

private const val NOTIFICATION_ID_FOREGROUND_SERVICE = 1
private const val NOTIFICATION_ID_UNLOCK_TAP = 2001
private const val GROUP_KEY_FOREGROUND = "com.firsty.bildtest.FOREGROUND_GROUP"
private const val GROUP_KEY_UNLOCK = "com.firsty.bildtest.UNLOCK_GROUP"

/**
 * Service that listens for the device being unlocked and starts the MainActivity.
 * This service is started by the BootUpReceiver when the device is booted and
 * runs in the foreground to ensure it is not killed by the system.
 *
 * If the start of the app is not wanted immediately after unlocking, use the WorkManager for the
 * Background Task instead, which is more battery efficient, reliable and works across reboots.
 */
class UnlockReceiverService : Service() {

    private lateinit var unlockReceiver: BroadcastReceiver

    override fun onCreate() {
        super.onCreate()
        Log.d("UnlockService", "Service created")

        // Setup foreground notification to prevent the service from being killed
        createNotificationChannels(this)
        startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, createServiceNotification())

        // Define the BroadcastReceiver for when the screen is being unlocked
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    Log.d("UnlockService", "User unlocked the device")
                    showUnlockNotification()
                }
            }
        }

        // Register the receiver dynamically
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        registerReceiver(unlockReceiver, filter)
    }

    /**
     * Unregister the receiver when the service is destroyed
     * to avoid memory leaks or dangling receivers.
     */
    override fun onDestroy() {
        unregisterReceiver(unlockReceiver)
        super.onDestroy()
        Log.d("UnlockService", "Service destroyed")
    }

    /**
     * Handles service start requests from other components, such as the BootUpReceiver.
     * Returns START_STICKY to ensure the service is restarted if it is killed by the system.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("UnlockService", "Start request received")

        // Always ensure foreground notification is active
        if (!isNotificationVisible()) {
            Log.d("UnlockService", "Notification not visible — re-issuing foreground notification")
            startForeground(NOTIFICATION_ID_FOREGROUND_SERVICE, createServiceNotification())
        }

        return START_STICKY
    }

    /**
     * Since this is a standalone foreground service, don't offer a binding interface.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Create notification channels for the foreground service to post notifications.
     * This is required for Android 8.O (API 26) and above.
     */
    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel for the foreground service to keep service running
            val serviceChannel = NotificationChannel(
                "foreground_service_channel",
                "Unlock Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Channel for the Unlock Receiver Service to run in the foreground"
                enableLights(false)
                enableVibration(false)
                // Try to disable showing badge on the app icon showing the number of notifications
                // Only works reliably on Android 8.0 on Google and Samsung devices
                setShowBadge(false)
            }

            // Channel for the unlock notification to open the app to keep it separate from the service
            val unlockChannel = NotificationChannel(
                "unlock_channel",
                "Open App Action",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for notifications when the device is unlocked"
                enableLights(true)
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
            ShortcutBadger.removeCount(context)
            manager.createNotificationChannel(unlockChannel)
        }
    }

    /**
     * Create a notification object as part of the foreground service.
     * This notification is shown in the notification bar while the service is running
     * (to keep it running).
     */
    private fun createServiceNotification(): Notification {
        return NotificationCompat.Builder(this, "foreground_service_channel")
            .setContentTitle(Strings.FOREGROUND_SERVICE_TITLE)
            .setContentText(Strings.FOREGROUND_SERVICE_TEXT)
            .setSmallIcon(R.drawable.small_icon)
            .setOngoing(true)
            .setGroup(GROUP_KEY_FOREGROUND)
            .setGroupSummary(true)  // Needed for foreground service notifications
            .build()
    }

    private fun showUnlockNotification() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, "unlock_channel")
            .setContentTitle(Strings.UNLOCK_NOTIFICATION_TITLE)
            .setContentText(Strings.UNLOCK_NOTIFICATION_TEXT)
            .setSmallIcon(R.drawable.small_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Must be high to show heads-up notification
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Makes it vibrate/make sound
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // Helps system know it's important
            .setGroup(GROUP_KEY_UNLOCK)
            .setGroupSummary(false)  // Prevents this notification from being a summary
            .setFullScreenIntent(pendingIntent, true) // For heads-up notification guarantee
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) { return }
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID_UNLOCK_TAP, notification)
    }

    /**
     * Check if the foreground service notification is currently visible.
     */
    private fun isNotificationVisible(): Boolean {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        return manager.activeNotifications.any { it.id == NOTIFICATION_ID_FOREGROUND_SERVICE }
    }
}
