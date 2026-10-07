package com.demetrecerrone.astralforge

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

object PlayerIdentityStore {
    private const val PREFS = "astralforge_player_identity"
    private const val KEY_NAME = "player_name"

    fun getName(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_NAME, null)?.trim()
        if (!saved.isNullOrBlank()) return saved

        val user = FirebaseAuth.getInstance().currentUser
        return when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }
    }

    fun saveName(context: Context, rawName: String, onComplete: ((Boolean) -> Unit)? = null) {
        val name = rawName.trim().replace(Regex("\\s+"), " ").take(24)
        if (name.length < 2) {
            onComplete?.invoke(false)
            return
        }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_NAME, name)
            .apply()

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && !user.isAnonymous) {
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()

            user.updateProfile(request)
                .addOnCompleteListener { task ->
                    onComplete?.invoke(task.isSuccessful)
                }
        } else {
            onComplete?.invoke(true)
        }
    }
}
