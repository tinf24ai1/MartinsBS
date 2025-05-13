package com.firsty.bildtest.core.receivers

import android.content.ActivityNotFoundException
import com.firsty.bildtest.MainActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


//
// NOT RECEIVABLE BY APPS STARTED BY THE USER (Implicit receivers)
//
class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            Log.i("UnlockReceiver", "Device unlocked, starting app with Receiver-PID " + android.os.Process.myPid())
            val openAppIntent = Intent(context, MainActivity::class.java)
            openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            try {
                context.startActivity(openAppIntent)
            } catch (e: ActivityNotFoundException) {
                Log.e("UnlockReceiver", "Activity not found trying to start app: ${e.message}")
            } catch (e: Exception) {
                Log.e("UnlockReceiver", "Unknown error trying to start app: ${e.message}")
            }
        }
    }
}