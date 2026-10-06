package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlin.math.max
import kotlin.math.min

class MainHubActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private val uiHandler = Handler(Looper.getMainLooper())
    private var playerListener: ListenerRegistration? = null
    private var eventEndAtMillis: Long = 0L

    private lateinit var avatarText: TextView
    private lateinit var playerNameText: TextView
    private lateinit var playerLevelText: TextView
    private lateinit var playerPowerText: TextView
    private lateinit var playerXpBar: ProgressBar
    private lateinit var playerXpText: TextView
    private lateinit var mailBadge: TextView
    private lateinit var notificationBadge: TextView
    private lateinit var eventTitleText: TextView
    private lateinit var eventCountdownText: TextView
    private lateinit var questOneProgress: TextView
    private lateinit var questOneBar: ProgressBar
    private lateinit var questTwoProgress: TextView
    private lateinit var questTwoBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = auth.currentUser
        if (user == null) {
            returnToLogin()
            return
        }

        val namedHub = resources.getIdentifier("main_hub_exact", "drawable", packageName)
        val uploadedHub = resources.getIdentifier(
            "file_0000000080b481f587bdf0c5eea2bf2c",
            "drawable",
            packageName
        )
        val hubDrawableId = if (namedHub != 0) namedHub else uploadedHub

        if (hubDrawableId == 0) {
            Toast.makeText(this, "Main hub artwork was not found.", Toast.LENGTH_LONG).show()
            return
        }

        showExactHub(hubDrawableId)
        initializePlayerIfNeeded(user.uid)
        observePlayer(user.uid)
    }

    override fun onDestroy() {
        playerListener?.remove()
        uiHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
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

        val overlay = ReferenceOverlayLayout(this, 941f, 1672f)

        addPlayerPanel(overlay)
        addUtilityIcons(overlay)
        addEventBanner(overlay)
        addActiveQuests(overlay)
        addHubHotspots(overlay)

        screen.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(screen)
    }

    private fun addPlayerPanel(overlay: ReferenceOverlayLayout) {
        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), dp(7), dp(12), dp(7))
            background = panelBackground(16f, 2)
            isClickable = true
            setOnClickListener { openSection(GameSectionActivity.SECTION_CHARACTER) }
        }

        val avatarFrame = FrameLayout(this).apply {
            background = circleBackground(
                Color.argb(225, 6, 16, 42),
                Color.rgb(232, 201, 123),
                2
            )
        }

        avatarText = TextView(this).apply {
            text = "A"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 28f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(10f, 0f, 0f, Color.rgb(77, 110, 255))
        }
        avatarFrame.addView(
            avatarText,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        panel.addView(
            avatarFrame,
            LinearLayout.LayoutParams(dp(72), dp(72)).apply {
                marginEnd = dp(10)
            }
        )

        val info = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }

        playerNameText = TextView(this).apply {
            text = "Player"
            setTextColor(Color.WHITE)
            textSize = 18f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            maxLines = 1
        }
        playerLevelText = TextView(this).apply {
            text = "Lv. 1"
            setTextColor(Color.rgb(232, 224, 255))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
        }
        playerXpBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressTintList = ColorStateList.valueOf(Color.rgb(79, 219, 255))
            progressBackgroundTintList = ColorStateList.valueOf(Color.argb(150, 23, 25, 54))
        }
        playerXpText = TextView(this).apply {
            text = "XP 0 / 100"
            setTextColor(Color.rgb(163, 218, 255))
            textSize = 9f
            gravity = Gravity.END
        }
        playerPowerText = TextView(this).apply {
            text = "⚔ 1,000"
            setTextColor(Color.rgb(247, 214, 112))
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }

        info.addView(playerNameText)
        info.addView(playerLevelText)
        info.addView(
            playerXpBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(6)
            ).apply { topMargin = dp(3) }
        )
        info.addView(playerXpText)
        info.addView(playerPowerText)

        panel.addView(
            info,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        )

        overlay.addMappedView(panel, 10f, 8f, 332f, 145f)
    }

    private fun addUtilityIcons(overlay: ReferenceOverlayLayout) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        val mail = utilityButton("✉", "Mail") {
            val count = badgeCount(mailBadge)
            val message = if (count > 0) {
                count.toString() + " unread message" + if (count == 1) "." else "s."
            } else {
                "No unread mail."
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        mailBadge = mail.second

        val notifications = utilityButton("●", "Alerts") {
            val count = badgeCount(notificationBadge)
            val message = if (count > 0) {
                count.toString() + " new notification" + if (count == 1) "." else "s."
            } else {
                "No new notifications."
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
        notificationBadge = notifications.second

        val settings = utilityButton("⚙", "Settings") {
            openSection(GameSectionActivity.SECTION_SETTINGS)
        }

        listOf(mail.first, notifications.first, settings.first).forEachIndexed { index, view ->
            row.addView(
                view,
                LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                    if (index > 0) marginStart = dp(4)
                }
            )
        }

        overlay.addMappedView(row, 776f, 38f, 936f, 112f)
    }

    private fun utilityButton(
        symbol: String,
        contentLabel: String,
        action: () -> Unit
    ): Pair<FrameLayout, TextView> {
        val frame = FrameLayout(this).apply {
            contentDescription = contentLabel
            background = panelBackground(9f, 1)
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }

        val icon = TextView(this).apply {
            text = symbol
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = if (symbol == "●") 18f else 22f
            typeface = Typeface.DEFAULT_BOLD
        }
        frame.addView(
            icon,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val badge = TextView(this).apply {
            text = "0"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            background = circleBackground(Color.rgb(225, 35, 72), Color.WHITE, 1)
            visibility = View.GONE
        }
        frame.addView(
            badge,
            FrameLayout.LayoutParams(dp(18), dp(18), Gravity.TOP or Gravity.END).apply {
                topMargin = dp(1)
                marginEnd = dp(1)
            }
        )
        return frame to badge
    }

    private fun addEventBanner(overlay: ReferenceOverlayLayout) {
        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(8), dp(12), dp(8))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.argb(235, 18, 7, 46),
                    Color.argb(235, 91, 42, 179),
                    Color.argb(235, 20, 8, 57)
                )
            ).apply {
                cornerRadius = dp(10).toFloat()
                setStroke(dp(2), Color.rgb(228, 198, 120))
            }
            isClickable = true
            setOnClickListener { openSection(GameSectionActivity.SECTION_CLASS) }
        }

        val kicker = TextView(this).apply {
            text = "✦  LIMITED EVENT  ✦"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(230, 207, 255))
            textSize = 9f
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.08f
        }
        eventTitleText = TextView(this).apply {
            text = "STARFALL\nSUMMONS"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 17f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            setShadowLayer(8f, 0f, 0f, Color.rgb(132, 71, 255))
        }
        eventCountdownText = TextView(this).apply {
            text = "Ends in --"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(231, 227, 245))
            textSize = 10f
        }
        val dots = TextView(this).apply {
            text = "●  ○  ○"
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(196, 170, 255))
            textSize = 10f
        }

        banner.addView(kicker)
        banner.addView(eventTitleText)
        banner.addView(eventCountdownText)
        banner.addView(dots)

        overlay.addMappedView(banner, 18f, 168f, 272f, 318f)
    }

    private data class QuestRowViews(
        val root: LinearLayout,
        val progressText: TextView,
        val bar: ProgressBar
    )

    private fun addActiveQuests(overlay: ReferenceOverlayLayout) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(8), dp(10), dp(8))
            background = panelBackground(8f, 2)
            isClickable = true
            setOnClickListener { openSection(GameSectionActivity.SECTION_INVENTORY) }
        }

        val header = TextView(this).apply {
            text = "✦  Active Quests                         »"
            setTextColor(Color.rgb(244, 232, 207))
            textSize = 13f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        card.addView(header)

        val first = questRow("◉", "Clear Depth 1–3", "◆ 50")
        questOneProgress = first.progressText
        questOneBar = first.bar
        card.addView(first.root)

        val divider = View(this).apply {
            setBackgroundColor(Color.argb(90, 228, 198, 120))
        }
        card.addView(
            divider,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(5)
                bottomMargin = dp(5)
            }
        )

        val second = questRow("▣", "Claim Daily Reward", "● 10,000")
        questTwoProgress = second.progressText
        questTwoBar = second.bar
        card.addView(second.root)

        overlay.addMappedView(card, 12f, 1056f, 340f, 1288f)
    }

    private fun questRow(
        icon: String,
        title: String,
        reward: String
    ): QuestRowViews {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(5), 0, dp(2))
        }

        val iconView = TextView(this).apply {
            text = icon
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(177, 113, 255))
            textSize = 21f
            background = panelBackground(6f, 1)
        }
        root.addView(
            iconView,
            LinearLayout.LayoutParams(dp(48), dp(48)).apply {
                marginEnd = dp(8)
            }
        )

        val center = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        val titleView = TextView(this).apply {
            text = title
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
        }
        val progress = TextView(this).apply {
            text = "0/1"
            setTextColor(Color.rgb(221, 219, 237))
            textSize = 10f
        }
        val bar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            progress = 0
            progressTintList = ColorStateList.valueOf(Color.rgb(124, 80, 255))
            progressBackgroundTintList = ColorStateList.valueOf(Color.argb(145, 29, 30, 54))
        }
        center.addView(titleView)
        center.addView(
            bar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(7)
            ).apply { topMargin = dp(2) }
        )
        center.addView(progress)

        root.addView(
            center,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )

        val rewardView = TextView(this).apply {
            text = reward
            gravity = Gravity.CENTER
            setTextColor(Color.rgb(248, 215, 112))
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            background = panelBackground(5f, 1)
            setPadding(dp(5), dp(4), dp(5), dp(4))
        }
        root.addView(
            rewardView,
            LinearLayout.LayoutParams(dp(68), LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                marginStart = dp(6)
            }
        )

        return QuestRowViews(root, progress, bar)
    }

    private fun addHubHotspots(overlay: ReferenceOverlayLayout) {
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

        hotspot(350f, 548f, 595f, 620f) { openSection(GameSectionActivity.SECTION_DESCEND) }
        hotspot(38f, 640f, 278f, 724f) { openSection(GameSectionActivity.SECTION_SHOP) }
        hotspot(676f, 642f, 934f, 730f) { openSection(GameSectionActivity.SECTION_EQUIPMENT) }
        hotspot(348f, 760f, 612f, 850f) { openSection(GameSectionActivity.SECTION_CLASS) }
        hotspot(112f, 986f, 367f, 1084f) { openSection(GameSectionActivity.SECTION_CHARACTER) }
        hotspot(675f, 928f, 929f, 1030f) { openSection(GameSectionActivity.SECTION_ASCENSION) }
        hotspot(700f, 1074f, 936f, 1180f) { openSection(GameSectionActivity.SECTION_INVENTORY) }
        hotspot(255f, 1340f, 686f, 1488f) { openSection(GameSectionActivity.SECTION_DESCEND) }

        hotspot(0f, 1502f, 184f, 1672f) { }
        hotspot(184f, 1502f, 372f, 1672f) { openSection(GameSectionActivity.SECTION_DESCEND) }
        hotspot(372f, 1502f, 558f, 1672f) { openSection(GameSectionActivity.SECTION_CHARACTER) }
        hotspot(558f, 1502f, 752f, 1672f) { openSection(GameSectionActivity.SECTION_EQUIPMENT) }
        hotspot(752f, 1502f, 941f, 1672f) { openSection(GameSectionActivity.SECTION_SETTINGS) }

        hotspot(742f, 5f, 785f, 43f) { openSection(GameSectionActivity.SECTION_SHOP) }
        hotspot(742f, 46f, 785f, 86f) { openSection(GameSectionActivity.SECTION_SHOP) }
        hotspot(742f, 88f, 785f, 130f) { openSection(GameSectionActivity.SECTION_SHOP) }
    }

    private fun initializePlayerIfNeeded(uid: String) {
        val defaultEventEnd =
            System.currentTimeMillis() + 12L * 24L * 60L * 60L * 1000L

        val defaults = mapOf(
            "level" to 1L,
            "xp" to 0L,
            "xpToNext" to 100L,
            "power" to 1000L,
            "rank" to "E",
            "gold" to 0L,
            "essence" to 0L,
            "className" to "Unawakened",
            "highestDepth" to 0L,
            "unreadMail" to 0L,
            "unreadNotifications" to 0L,
            "dailyRewardClaimed" to false,
            "featuredEventTitle" to "STARFALL SUMMONS",
            "featuredEventEndAt" to defaultEventEnd
        )

        val ref = firestore.collection("players").document(uid)
        ref.get().addOnSuccessListener { snapshot ->
            val missing = defaults.filterKeys { key -> !snapshot.contains(key) }
            if (missing.isNotEmpty()) {
                ref.set(missing, SetOptions.merge())
            }
        }
    }

    private fun observePlayer(uid: String) {
        playerListener?.remove()
        playerListener = firestore.collection("players")
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val fallbackName =
                    if (auth.currentUser?.isAnonymous == true) "Guest"
                    else auth.currentUser?.displayName
                        ?: auth.currentUser?.email?.substringBefore("@")
                        ?: "Player"

                val name = snapshot.getString("displayName") ?: fallbackName
                val level = snapshot.getLong("level") ?: 1L
                val xp = snapshot.getLong("xp") ?: 0L
                val xpToNext = max(1L, snapshot.getLong("xpToNext") ?: 100L)
                val power = snapshot.getLong("power") ?: 1000L
                val highestDepth = snapshot.getLong("highestDepth") ?: 0L
                val unreadMail = snapshot.getLong("unreadMail") ?: 0L
                val unreadNotifications = snapshot.getLong("unreadNotifications") ?: 0L
                val dailyRewardClaimed =
                    snapshot.getBoolean("dailyRewardClaimed") ?: false

                playerNameText.text = name
                avatarText.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "A"
                playerLevelText.text = "Lv. $level"
                playerPowerText.text = "⚔ " + formatNumber(power)

                val xpPercent =
                    ((xp.coerceAtMost(xpToNext) * 100L) / xpToNext).toInt()
                playerXpBar.progress = xpPercent
                playerXpText.text =
                    "XP " + formatNumber(xp) + " / " + formatNumber(xpToNext)

                setBadge(mailBadge, unreadMail)
                setBadge(notificationBadge, unreadNotifications)

                val title =
                    snapshot.getString("featuredEventTitle") ?: "STARFALL SUMMONS"
                eventTitleText.text = title.replaceFirst(" ", "\n")
                eventEndAtMillis =
                    snapshot.getLong("featuredEventEndAt")
                        ?: (System.currentTimeMillis() +
                            12L * 24L * 60L * 60L * 1000L)
                scheduleEventCountdown()

                val clearProgress = min(3L, highestDepth)
                questOneProgress.text = "$clearProgress/3"
                questOneBar.progress = ((clearProgress * 100L) / 3L).toInt()

                questTwoProgress.text =
                    if (dailyRewardClaimed) "1/1" else "0/1"
                questTwoBar.progress = if (dailyRewardClaimed) 100 else 0
            }
    }

    private fun scheduleEventCountdown() {
        uiHandler.removeCallbacks(eventCountdownRunnable)
        eventCountdownRunnable.run()
    }

    private val eventCountdownRunnable = object : Runnable {
        override fun run() {
            if (!::eventCountdownText.isInitialized) return

            val remaining =
                max(0L, eventEndAtMillis - System.currentTimeMillis())
            val totalMinutes = remaining / 60_000L
            val days = totalMinutes / (24L * 60L)
            val hours = (totalMinutes % (24L * 60L)) / 60L
            val minutes = totalMinutes % 60L

            eventCountdownText.text =
                when {
                    remaining <= 0L -> "Event ended"
                    days > 0L ->
                        "Ends in " + days + "d " + hours + "h"
                    else ->
                        "Ends in " + hours + "h " + minutes + "m"
                }

            if (remaining > 0L) {
                uiHandler.postDelayed(this, 60_000L)
            }
        }
    }

    private fun setBadge(view: TextView, count: Long) {
        if (count <= 0L) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = if (count > 99L) "99+" else count.toString()
        }
    }

    private fun badgeCount(view: TextView): Int =
        view.text.toString().filter { it.isDigit() }.toIntOrNull() ?: 0

    private fun openSection(section: String) {
        startActivity(
            Intent(this, GameSectionActivity::class.java)
                .putExtra(GameSectionActivity.EXTRA_SECTION, section)
        )
    }

    private fun panelBackground(radiusDp: Float, strokeDp: Int): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = dp(radiusDp).toFloat()
            setColor(Color.argb(205, 3, 8, 24))
            setStroke(dp(strokeDp), Color.rgb(220, 190, 118))
        }

    private fun circleBackground(
        fill: Int,
        stroke: Int,
        strokeDp: Int
    ): GradientDrawable =
        GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fill)
            setStroke(dp(strokeDp), stroke)
        }

    private fun formatNumber(value: Long): String =
        String.format(java.util.Locale.US, "%,d", value)

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun dp(value: Float): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun returnToLogin() {
        startActivity(
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}
