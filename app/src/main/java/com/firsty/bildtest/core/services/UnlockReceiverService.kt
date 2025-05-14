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
        createNotificationChannel()
        startForeground(1, createNotification())

        // Define the BroadcastReceiver for when the screen is being unlocked
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_USER_PRESENT) {
                    Log.d("UnlockService", "User unlocked the device")
                    // Old method
                    /*val launchIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(launchIntent)*/

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
        return START_STICKY
    }

    /**
     * Since this is a standalone foreground service, don't offer a binding interface.
     */
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Create a notification channel for the foreground service to post notifications.
     * This is required for Android 8.O (API 26) and above.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "unlock_channel",
                "Unlock Listener",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Create a notification object as part of the foreground service.
     * This notification is shown in the notification bar while the service is running
     * (to keep it running).
     */
    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "unlock_channel")
            .setContentTitle("ScreenSaver: Unlock monitor")
            .setContentText("Monitoring device unlocks to start ScreenSaver")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
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
            .setContentTitle("Device Unlocked")
            .setContentText("Tap to open app")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //      public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
            // to handle the case where the user grants the permission.
            // See the documentation for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this).notify(2001, notification)
    }
}
