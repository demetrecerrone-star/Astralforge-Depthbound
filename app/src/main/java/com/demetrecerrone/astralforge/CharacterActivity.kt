package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

class CharacterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val progress = ProgressionStore.load(this)
        val user = FirebaseAuth.getInstance().currentUser
        val name = when {
            user?.isAnonymous == true -> "Guest Delver"
            !user?.displayName.isNullOrBlank() -> user?.displayName ?: "Delver"
            else -> "Delver"
        }

        val root = FrameLayout(this)
        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.file_00000000bfc081f5b0a1931c13d627e8)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.72f
                contentDescription = null
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        root.addView(
            View(this).apply {
                setBackgroundColor(Color.argb(182, 2, 4, 18))
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
            setPadding(
                AuthUi.dp(this@CharacterActivity, 18),
                AuthUi.dp(this@CharacterActivity, 64),
                AuthUi.dp(this@CharacterActivity, 18),
                AuthUi.dp(this@CharacterActivity, 40)
            )
        }

        content.addView(titleText("CHARACTER", 28f))
        content.addView(titleText(name, 21f).apply {
            setTextColor(Color.rgb(213, 198, 255))
            setPadding(0, AuthUi.dp(this@CharacterActivity, 5), 0, 0)
        })
        content.addView(bodyText(progress.title).apply {
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@CharacterActivity, 4), 0, AuthUi.dp(this@CharacterActivity, 16))
        })

        val identity = panel()
        identity.addView(row("LEVEL", progress.level.toString()))
        identity.addView(row("RANK", progress.rank))
        identity.addView(row("CLASS", progress.playerClass))
        identity.addView(row("CURRENT DEPTH", progress.depth.toString()))
        identity.addView(row("POWER RATING", progress.powerRating.toString()))
        content.addView(identity, panelParams())

        val xpPanel = panel()
        xpPanel.addView(sectionLabel("EXPERIENCE"))
        xpPanel.addView(bodyText("${progress.xp} / ${progress.xpToNext} XP").apply {
            gravity = Gravity.CENTER
        })
        xpPanel.addView(ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = progress.xpToNext.coerceAtLeast(1)
            this.progress = progress.xp.coerceIn(0, max)
            progressTintList = ColorStateList.valueOf(Color.rgb(145, 72, 255))
            progressBackgroundTintList = ColorStateList.valueOf(Color.rgb(34, 27, 64))
        }, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            AuthUi.dp(this, 14)
        ).apply {
            topMargin = AuthUi.dp(this@CharacterActivity, 10)
        })
        xpPanel.addView(bodyText("Unspent Attribute Points: ${progress.attributePoints}").apply {
            gravity = Gravity.CENTER
            setPadding(0, AuthUi.dp(this@CharacterActivity, 10), 0, 0)
        })
        content.addView(xpPanel, panelParams())

        val attributes = panel()
        attributes.addView(sectionLabel("CORE ATTRIBUTES"))
        attributes.addView(row("STRENGTH", progress.strength.toString()))
        attributes.addView(row("VITALITY", progress.vitality.toString()))
        attributes.addView(row("AGILITY", progress.agility.toString()))
        attributes.addView(row("INTELLIGENCE", progress.intelligence.toString()))
        attributes.addView(row("LUCK", progress.luck.toString()))
        content.addView(attributes, panelParams())

        val combat = panel()
        combat.addView(sectionLabel("COMBAT STATS"))
        combat.addView(row("MAX HP", progress.maxHp.toString()))
        combat.addView(row("ATTACK", progress.attack.toString()))
        combat.addView(row("DEFENSE", progress.defense.toString()))
        combat.addView(row("MAGIC POWER", progress.magicPower.toString()))
        combat.addView(row("CRIT CHANCE", "${progress.critChance}%"))
        content.addView(combat, panelParams())

        val wallet = panel()
        wallet.addView(sectionLabel("CURRENCIES"))
        wallet.addView(row("GOLD", progress.gold.toString()))
        wallet.addView(row("ASTRAL SHARDS", progress.astralShards.toString()))
        content.addView(wallet, panelParams())

        content.addView(bodyText("Use Android Back to return to the hub.").apply {
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(145, 133, 184))
            setPadding(0, AuthUi.dp(this@CharacterActivity, 8), 0, 0)
        })

        scroll.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
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

    private fun titleText(value: String, size: Float) = TextView(this).apply {
        text = value
        textSize = size
        setTextColor(Color.WHITE)
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        gravity = Gravity.CENTER
        letterSpacing = 0.05f
    }

    private fun bodyText(value: String) = TextView(this).apply {
        text = value
        textSize = 14f
        setTextColor(Color.rgb(204, 195, 232))
    }

    private fun sectionLabel(value: String) = TextView(this).apply {
        text = value
        textSize = 16f
        setTextColor(Color.WHITE)
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.08f
        gravity = Gravity.CENTER
        setPadding(0, 0, 0, AuthUi.dp(this@CharacterActivity, 8))
    }

    private fun row(label: String, value: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, AuthUi.dp(this@CharacterActivity, 7), 0, AuthUi.dp(this@CharacterActivity, 7))

            addView(TextView(this@CharacterActivity).apply {
                text = label
                textSize = 13f
                setTextColor(Color.rgb(168, 156, 205))
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            addView(TextView(this@CharacterActivity).apply {
                text = value
                textSize = 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.END
            })
        }
    }

    private fun panel(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(
            AuthUi.dp(this@CharacterActivity, 18),
            AuthUi.dp(this@CharacterActivity, 16),
            AuthUi.dp(this@CharacterActivity, 18),
            AuthUi.dp(this@CharacterActivity, 16)
        )
        background = GradientDrawable().apply {
            setColor(Color.argb(220, 5, 9, 28))
            cornerRadius = AuthUi.dp(this@CharacterActivity, 16).toFloat()
            setStroke(AuthUi.dp(this@CharacterActivity, 1), Color.rgb(119, 86, 205))
        }
    }

    private fun panelParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        topMargin = AuthUi.dp(this@CharacterActivity, 12)
    }
}
