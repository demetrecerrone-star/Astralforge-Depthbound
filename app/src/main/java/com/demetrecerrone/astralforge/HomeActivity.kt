package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
                scaleType = ImageView.ScaleType.CENTER_CROP
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            View(this).apply {
                background = GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(
                        Color.argb(95, 2, 4, 18),
                        Color.argb(160, 2, 4, 18),
                        Color.argb(225, 2, 4, 18)
                    )
                )
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val scroll = ScrollView(this).apply {
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 66),
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 40)
            )
        }

        val user = FirebaseAuth.getInstance().currentUser
        val displayName = when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }

        content.addView(TextView(this).apply {
            text = "ASTRAL FORGE: DEPTHBOUND"
            textSize = 24f
            setTextColor(Color.WHITE)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.04f
        })

        content.addView(TextView(this).apply {
            text = displayName
            textSize = 18f
            setTextColor(Color.rgb(218, 205, 255))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@HomeActivity, 8), 0, 0)
        })

        val status = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 14),
                AuthUi.dp(this@HomeActivity, 18),
                AuthUi.dp(this@HomeActivity, 14)
            )
            background = GradientDrawable().apply {
                setColor(Color.argb(205, 6, 9, 28))
                cornerRadius = AuthUi.dp(this@HomeActivity, 16).toFloat()
                setStroke(AuthUi.dp(this@HomeActivity, 1), Color.rgb(119, 86, 205))
            }
        }
        status.addView(TextView(this).apply {
            text = "LEVEL 1   •   DEPTH 1"
            textSize = 15f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        })
        status.addView(TextView(this).apply {
            text = "0 / 100 XP     ✦ 0 Astral Shards     ⬡ 0 Gold"
            textSize = 12f
            setTextColor(Color.rgb(194, 185, 230))
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@HomeActivity, 7), 0, 0)
        })
        content.addView(
            status,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 14)
                bottomMargin = AuthUi.dp(this@HomeActivity, 14)
            }
        )

        addHubButton(content, "continue_adventure.png", "Continue Adventure", 84) {
            openSection("CONTINUE ADVENTURE", "Your next descent into the Depthbound awaits.")
        }
        addHubButton(content, "character.png", "Character") {
            openSection("CHARACTER", "Attributes, level progression, identity, and combat statistics.")
        }
        addHubButton(content, "equipment.png", "Equipment") {
            openSection("EQUIPMENT", "Weapons, armor, accessories, loadouts, and item power.")
        }
        addHubButton(content, "class.png", "Class") {
            openSection("CLASS", "Class progression, skills, evolutions, and hidden paths.")
        }
        addHubButton(content, "ascension.png", "Ascension") {
            openSection("ASCENSION", "Permanent advancement beyond ordinary level limits.")
        }
        addHubButton(content, "forge.png", "Forge") {
            openSection("FORGE", "Upgrade, enhance, refine, and craft equipment.")
        }
        addHubButton(content, "quests.png", "Quests") {
            openSection("QUESTS", "Main objectives, side quests, dailies, and rewards.")
        }
        addHubButton(content, "shop.png", "Shop") {
            openSection("SHOP", "Currency, consumables, cosmetics, and future store systems.")
        }
        addHubButton(content, "inventory.png", "Inventory") {
            openSection("INVENTORY", "Items, materials, loot, keys, and consumables.")
        }
        addHubButton(content, "achievements.png", "Achievements") {
            openSection("ACHIEVEMENTS", "Milestones, account accomplishments, and special rewards.")
        }
        addHubButton(content, "settings.png", "Settings") {
            openSection("SETTINGS", "Audio, display, account, accessibility, and game preferences.")
        }
        addHubButton(content, "sign_out.png", "Sign Out") {
            FirebaseAuth.getInstance().signOut()
            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        scroll.addView(
            content,
            ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
            )
        )
        root.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(root)
    }

    private fun addHubButton(
        parent: LinearLayout,
        fileName: String,
        description: String,
        heightDp: Int = 72,
        onClick: () -> Unit
    ) {
        val button = AuthUi.zipAssetButton(
            this,
            fileName,
            description,
            heightDp,
            onClick
        )
        parent.addView(
            button,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                AuthUi.dp(this, heightDp)
            ).apply {
                topMargin = AuthUi.dp(this@HomeActivity, 5)
            }
        )
    }

    private fun openSection(title: String, subtitle: String) {
        startActivity(
            Intent(this, GameSectionActivity::class.java)
                .putExtra("section_title", title)
                .putExtra("section_subtitle", subtitle)
        )
    }
}
