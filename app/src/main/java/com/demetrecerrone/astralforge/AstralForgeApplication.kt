package com.demetrecerrone.astralforge

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager

class AstralForgeApplication : Application(), Application.ActivityLifecycleCallbacks {

    private var activeGameActivities = 0

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
    }

    private fun isGameActivity(activity: Activity): Boolean {
        return activity !is MainActivity && activity !is CreateAccountActivity
    }

    private fun applyImmersiveFullscreen(activity: Activity) {
        val window = activity.window
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let { controller ->
                controller.hide(
                    WindowInsets.Type.statusBars() or
                        WindowInsets.Type.navigationBars()
                )
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        applyImmersiveFullscreen(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        applyImmersiveFullscreen(activity)
    }

    override fun onActivityPaused(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
