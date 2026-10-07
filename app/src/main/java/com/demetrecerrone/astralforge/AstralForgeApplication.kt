package com.demetrecerrone.astralforge

import android.app.Activity
import android.app.Application
import android.os.Bundle

class AstralForgeApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var activeGameActivities = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    private fun isGameActivity(activity: Activity): Boolean {
        return activity !is MainActivity && activity !is CreateAccountActivity
    }

    override fun onActivityStarted(activity: Activity) {
        if (!isGameActivity(activity)) return
        activeGameActivities += 1
        AppMusicManager.sync(activity)
    }

    override fun onActivityStopped(activity: Activity) {
        if (!isGameActivity(activity)) return
        activeGameActivities = (activeGameActivities - 1).coerceAtLeast(0)
        if (activeGameActivities == 0) {
            AppMusicManager.pause()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityResumed(activity: Activity) = Unit
    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
