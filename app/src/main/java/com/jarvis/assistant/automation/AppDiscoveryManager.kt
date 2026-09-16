package com.jarvis.assistant.automation

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

data class InstalledApp(
    val label: String,
    val packageName: String
)

class AppDiscoveryManager(private val context: Context) {

    fun findAndLaunchApp(appName: String): AppLaunchResult {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val query = appName.lowercase().trim()

        val matchingApps = mutableListOf<InstalledApp>()

        for (app in apps) {
            // Filter non-launchable or system utilities without launch intents
            val launchIntent = pm.getLaunchIntentForPackage(app.packageName) ?: continue
            val label = pm.getApplicationLabel(app).toString().lowercase()

            if (label == query) {
                // Exact match
                launchAppByPackage(app.packageName)
                return AppLaunchResult.Launched(pm.getApplicationLabel(app).toString())
            } else if (label.contains(query) || query.contains(label)) {
                matchingApps.add(InstalledApp(pm.getApplicationLabel(app).toString(), app.packageName))
            }
        }

        return when {
            matchingApps.size == 1 -> {
                launchAppByPackage(matchingApps.first().packageName)
                AppLaunchResult.Launched(matchingApps.first().label)
            }
            matchingApps.size > 1 -> {
                AppLaunchResult.DisambiguationRequired(matchingApps.map { it.label })
            }
            else -> {
                AppLaunchResult.NotFound(appName)
            }
        }
    }

    private fun launchAppByPackage(packageName: String): Boolean {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }
}

sealed class AppLaunchResult {
    data class Launched(val appName: String) : AppLaunchResult()
    data class DisambiguationRequired(val candidates: List<String>) : AppLaunchResult()
    data class NotFound(val requestedName: String) : AppLaunchResult()
}
