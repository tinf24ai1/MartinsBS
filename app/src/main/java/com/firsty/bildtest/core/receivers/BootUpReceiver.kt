package com.firsty.bildtest.core.receivers

import android.content.ActivityNotFoundException
import com.firsty.bildtest.MainActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

import com.firsty.bildtest.core.services.UnlockReceiverService

/**
 * BootUpReceiver is a BroadcastReceiver that listens for the ACTION_BOOT_COMPLETED event.
 * When the device is booted, it starts the UnlockReceiverService and opens the MainActivity.
 * This is used to ensure that the app is opened after the device is booted and continues to be
 * started every time the user unlocks the screen.
 */
class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootUpReceiver", "BS: Device booted, Receiver-PID " + android.os.Process.myPid())

            // Start foreground service, that registers a receiver for ACTION_USER_PRESENT
            // to open the app when the user unlocks the device
            val serviceIntent = Intent(context, UnlockReceiverService::class.java)
            ContextCompat.startForegroundService(context, serviceIntent)

            // Start app after first boot
            val openAppIntent = Intent(context, MainActivity::class.java)
            openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(openAppIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("BootUpReceiver", "BS: Failed to start app: ${e.message}")
            } catch (e: Exception) {
                Log.e("BootUpReceiver", "BS: An unexpected error occurred: ${e.message}")
            }
        }
    }
}