package com.demetrecerrone.astralforge

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest

/**
 * Keeps local game data isolated per Firebase account.
 *
 * Legacy unscoped preferences are claimed once by the first signed-in account
 * after this migration so an existing local save is preserved without leaking
 * into every other account used on the same device.
 */
object AccountScopedStorage {
    private const val MIGRATION_PREFS = "astralforge_account_scope_migration"
    private const val LEGACY_OWNER_PREFIX = "legacy_owner_"

    fun preferences(context: Context, baseName: String): SharedPreferences {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "signed_out"
        val accountKey = stableAccountKey(uid)
        val scopedName = baseName + "_account_" + accountKey
        val scoped = context.getSharedPreferences(scopedName, Context.MODE_PRIVATE)

        migrateLegacyOnce(
            context = context,
            baseName = baseName,
            uid = uid,
            scoped = scoped
        )

        return scoped
    }

    private fun migrateLegacyOnce(
        context: Context,
        baseName: String,
        uid: String,
        scoped: SharedPreferences
    ) {
        if (uid == "signed_out") return

        val migration = context.getSharedPreferences(
            MIGRATION_PREFS,
            Context.MODE_PRIVATE
        )
        val ownerKey = LEGACY_OWNER_PREFIX + baseName
        val existingOwner = migration.getString(ownerKey, null)

        if (existingOwner != null && existingOwner != uid) return
        if (scoped.all.isNotEmpty()) {
            if (existingOwner == null) {
                migration.edit().putString(ownerKey, uid).apply()
            }
            return
        }

        val legacy = context.getSharedPreferences(baseName, Context.MODE_PRIVATE)
        if (legacy.all.isNotEmpty()) {
            val editor = scoped.edit()
            legacy.all.forEach { (key, value) ->
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Set<*> -> {
                        val strings = value.filterIsInstance<String>().toSet()
                        editor.putStringSet(key, strings)
                    }
                }
            }
            editor.apply()
        }

        migration.edit().putString(ownerKey, uid).apply()
    }

    private fun stableAccountKey(uid: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(uid.toByteArray(Charsets.UTF_8))
        return bytes.take(12).joinToString("") { "%02x".format(it) }
    }
}
