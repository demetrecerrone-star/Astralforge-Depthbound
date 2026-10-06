package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainHubActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var welcomeText: TextView
    private lateinit var statsText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            returnToLogin()
            return
        }

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(34), dp(22), dp(32))
        }

        val title = TextView(this).apply {
            text = getString(R.string.hub_title)
            setTextColor(Color.rgb(236, 230, 255))
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.hub_subtitle)
            setTextColor(Color.rgb(173, 163, 214))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(22))
        }

        welcomeText = TextView(this).apply {
            text = getString(R.string.hub_loading_player)
            setTextColor(Color.WHITE)
            textSize = 21f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(14), dp(12), dp(8))
        }

        statsText = TextView(this).apply {
            text = getString(R.string.hub_default_stats)
            setTextColor(Color.rgb(199, 190, 232))
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(4), dp(12), dp(18))
        }

        val section = TextView(this).apply {
            text = getString(R.string.hub_actions)
            setTextColor(Color.rgb(181, 118, 255))
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(10))
        }

        fun hubButton(label: Int, section: String): Button =
            Button(this).apply {
                text = getString(label)
                setOnClickListener {
                    startActivity(
                        Intent(this@MainHubActivity, GameSectionActivity::class.java)
                            .putExtra(GameSectionActivity.EXTRA_SECTION, section)
                    )
                }
            }

        val descend = hubButton(R.string.hub_descend, GameSectionActivity.SECTION_DESCEND)
        val character = hubButton(R.string.hub_character, GameSectionActivity.SECTION_CHARACTER)
        val equipment = hubButton(R.string.hub_equipment, GameSectionActivity.SECTION_EQUIPMENT)
        val classScreen = hubButton(R.string.hub_class, GameSectionActivity.SECTION_CLASS)
        val ascension = hubButton(R.string.hub_ascension, GameSectionActivity.SECTION_ASCENSION)
        val inventory = hubButton(R.string.hub_inventory, GameSectionActivity.SECTION_INVENTORY)
        val shop = hubButton(R.string.hub_shop, GameSectionActivity.SECTION_SHOP)
        val settings = hubButton(R.string.hub_settings, GameSectionActivity.SECTION_SETTINGS)

        val logout = Button(this).apply {
            text = getString(R.string.hub_log_out)
            setOnClickListener {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("remember_me", false)
                    .apply()
                auth.signOut()
                returnToLogin()
            }
        }

        fun addFullWidth(view: android.view.View, topMargin: Int = 8) {
            root.addView(
                view,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { this.topMargin = dp(topMargin) }
            )
        }

        root.addView(title)
        root.addView(subtitle)
        addFullWidth(welcomeText, 0)
        addFullWidth(statsText, 0)
        root.addView(section)

        listOf(
            descend,
            character,
            equipment,
            classScreen,
            ascension,
            inventory,
            shop,
            settings
        ).forEach { addFullWidth(it) }

        addFullWidth(logout, 18)

        scroll.addView(root)
        setContentView(scroll)

        initializeAndLoadPlayer(user.uid)
    }

    private fun initializeAndLoadPlayer(uid: String) {
        val defaults = mapOf(
            "level" to 1L,
            "xp" to 0L,
            "rank" to "E",
            "gold" to 0L,
            "essence" to 0L,
            "className" to "Unawakened",
            "highestDepth" to 0L
        )

        val ref = firestore.collection("players").document(uid)
        ref.set(defaults, SetOptions.merge())
            .addOnCompleteListener {
                ref.get()
                    .addOnSuccessListener { snapshot ->
                        val fallbackName =
                            if (auth.currentUser?.isAnonymous == true) "Guest"
                            else auth.currentUser?.displayName
                                ?: auth.currentUser?.email?.substringBefore("@")
                                ?: "Player"

                        val name = snapshot.getString("displayName") ?: fallbackName
                        val level = snapshot.getLong("level") ?: 1L
                        val rank = snapshot.getString("rank") ?: "E"
                        val gold = snapshot.getLong("gold") ?: 0L
                        val essence = snapshot.getLong("essence") ?: 0L
                        val className = snapshot.getString("className") ?: "Unawakened"
                        val depth = snapshot.getLong("highestDepth") ?: 0L

                        welcomeText.text = getString(R.string.hub_welcome, name)
                        statsText.text = getString(
                            R.string.hub_stats_format,
                            level,
                            rank,
                            className,
                            gold,
                            essence,
                            depth
                        )
                    }
                    .addOnFailureListener {
                        welcomeText.text = getString(R.string.hub_welcome, "Player")
                        statsText.text = getString(R.string.hub_default_stats)
                    }
            }
    }

    private fun returnToLogin() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
