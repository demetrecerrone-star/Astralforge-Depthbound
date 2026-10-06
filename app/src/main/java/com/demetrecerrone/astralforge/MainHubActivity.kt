package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainHubActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            returnToLogin()
            return
        }

        val exactHubId = resources.getIdentifier(
            "main_hub_exact",
            "drawable",
            packageName
        )

        if (exactHubId != 0) {
            showExactHub(exactHubId)
        } else {
            showFallbackHub()
        }

        initializePlayerIfNeeded(user.uid)
    }

    private fun showExactHub(hubDrawableId: Int) {
        val screen = FrameLayout(this)

        val background = ImageView(this).apply {
            setImageResource(hubDrawableId)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        screen.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val overlay = ReferenceOverlayLayout(
            this,
            941f,
            1672f
        )

        fun open(section: String) {
            startActivity(
                Intent(this, GameSectionActivity::class.java)
                    .putExtra(GameSectionActivity.EXTRA_SECTION, section)
            )
        }

        fun hotspot(
            left: Float,
            top: Float,
            right: Float,
            bottom: Float,
            action: () -> Unit
        ) {
            val view = View(this).apply {
                setBackgroundColor(Color.TRANSPARENT)
                isClickable = true
                setOnClickListener { action() }
            }
            overlay.addMappedView(view, left, top, right, bottom)
        }

        // Top profile and event card.
        hotspot(10f, 8f, 330f, 135f) {
            open(GameSectionActivity.SECTION_CHARACTER)
        }
        hotspot(16f, 175f, 268f, 310f) {
            open(GameSectionActivity.SECTION_CLASS)
        }

        // Main city destinations.
        hotspot(350f, 548f, 595f, 620f) {
            open(GameSectionActivity.SECTION_DESCEND)
        }
        hotspot(38f, 640f, 278f, 724f) {
            open(GameSectionActivity.SECTION_SHOP)
        }
        hotspot(676f, 642f, 934f, 730f) {
            open(GameSectionActivity.SECTION_EQUIPMENT)
        }
        hotspot(348f, 760f, 612f, 850f) {
            open(GameSectionActivity.SECTION_CLASS)
        }
        hotspot(112f, 986f, 367f, 1084f) {
            open(GameSectionActivity.SECTION_CHARACTER)
        }
        hotspot(675f, 928f, 929f, 1030f) {
            open(GameSectionActivity.SECTION_ASCENSION)
        }
        hotspot(700f, 1074f, 936f, 1180f) {
            open(GameSectionActivity.SECTION_INVENTORY)
        }

        // Large battle call-to-action.
        hotspot(255f, 1340f, 686f, 1488f) {
            open(GameSectionActivity.SECTION_DESCEND)
        }

        // Bottom navigation.
        hotspot(0f, 1502f, 184f, 1672f) {
            // Already on Home.
        }
        hotspot(184f, 1502f, 372f, 1672f) {
            open(GameSectionActivity.SECTION_DESCEND)
        }
        hotspot(372f, 1502f, 558f, 1672f) {
            open(GameSectionActivity.SECTION_CHARACTER)
        }
        hotspot(558f, 1502f, 752f, 1672f) {
            open(GameSectionActivity.SECTION_EQUIPMENT)
        }
        hotspot(752f, 1502f, 941f, 1672f) {
            open(GameSectionActivity.SECTION_SETTINGS)
        }

        // Top-right utility icons.
        hotspot(868f, 38f, 936f, 108f) {
            open(GameSectionActivity.SECTION_SETTINGS)
        }
        hotspot(778f, 38f, 826f, 108f) {
            Toast.makeText(this, "Mail is coming soon.", Toast.LENGTH_SHORT).show()
        }
        hotspot(824f, 38f, 870f, 108f) {
            Toast.makeText(this, "Notifications are coming soon.", Toast.LENGTH_SHORT).show()
        }

        // Currency/energy plus buttons route to the shop for now.
        hotspot(742f, 5f, 785f, 43f) {
            open(GameSectionActivity.SECTION_SHOP)
        }
        hotspot(742f, 46f, 785f, 86f) {
            open(GameSectionActivity.SECTION_SHOP)
        }
        hotspot(742f, 88f, 785f, 130f) {
            open(GameSectionActivity.SECTION_SHOP)
        }

        screen.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(screen)
    }

    private fun showFallbackHub() {
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
        root.addView(title)

        fun addButton(label: String, section: String) {
            root.addView(
                Button(this).apply {
                    text = label
                    setOnClickListener {
                        startActivity(
                            Intent(this@MainHubActivity, GameSectionActivity::class.java)
                                .putExtra(GameSectionActivity.EXTRA_SECTION, section)
                        )
                    }
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(10) }
            )
        }

        addButton("Dungeons / Battle", GameSectionActivity.SECTION_DESCEND)
        addButton("Heroes", GameSectionActivity.SECTION_CHARACTER)
        addButton("Forge / Gear", GameSectionActivity.SECTION_EQUIPMENT)
        addButton("Summon / Class", GameSectionActivity.SECTION_CLASS)
        addButton("Guild / Ascension", GameSectionActivity.SECTION_ASCENSION)
        addButton("Quests / Inventory", GameSectionActivity.SECTION_INVENTORY)
        addButton("Shop", GameSectionActivity.SECTION_SHOP)
        addButton("Menu / Settings", GameSectionActivity.SECTION_SETTINGS)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun initializePlayerIfNeeded(uid: String) {
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
        ref.get()
            .addOnSuccessListener { snapshot ->
                val missing = defaults.filterKeys { key -> !snapshot.contains(key) }
                if (missing.isNotEmpty()) {
                    ref.set(missing, SetOptions.merge())
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
