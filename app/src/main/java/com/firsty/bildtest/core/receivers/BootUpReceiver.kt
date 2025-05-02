package com.firsty.bildtest.core.receivers

import com.firsty.bildtest.MainActivity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {

            val openAppIntent = Intent(context, MainActivity::class.java)
            openAppIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(openAppIntent)
        }
    }
}