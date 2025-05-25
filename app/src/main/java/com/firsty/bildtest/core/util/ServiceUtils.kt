package com.firsty.bildtest.core.util

import android.app.ActivityManager
import android.content.Context

/**
 * Check if a service is running by using ActivityManager (top-level function).
 * This function is deprecated in API level 26 and above, but works for own services.
 * Currently unusued.
 */
@Suppress("DEPRECATION")
fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.getRunningServices(Int.MAX_VALUE)
        .any { it.service.className == serviceClass.name }
}