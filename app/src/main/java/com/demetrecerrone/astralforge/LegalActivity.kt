package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class LegalActivity : Activity() {

    companion object {
        const val EXTRA_PAGE = "legal_page"
        const val PAGE_PRIVACY = "privacy"
        const val PAGE_TERMS = "terms"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)

        val page = intent.getStringExtra(EXTRA_PAGE) ?: PAGE_PRIVACY
        val isPrivacy = page == PAGE_PRIVACY

        val root = FrameLayout(this)

        root.addView(
            ImageView(this).apply {
                setImageResource(R.drawable.hub_background_rebuild)
                scaleType = ImageView.ScaleType.CENTER_CROP
                alpha = 0.70f
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
                        Color.argb(170, 3, 4, 18),
                        Color.argb(218, 4, 5, 22),
                        Color.argb(240, 3, 4, 16)
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
            isFillViewport = true
        }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                AuthUi.dp(this@LegalActivity, 24),
                AuthUi.dp(this@LegalActivity, 28),
                AuthUi.dp(this@LegalActivity, 24),
                AuthUi.dp(this@LegalActivity, 34)
            )
        }

        content.addView(
            TextView(this).apply {
                text = if (isPrivacy) "PRIVACY POLICY" else "TERMS & LEGAL"
                textSize = 29f
                setTextColor(Color.WHITE)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                gravity = Gravity.CENTER
                letterSpacing = 0.07f
                setShadowLayer(10f, 0f, 0f, Color.rgb(123, 76, 255))
            }
        )

        content.addView(
            TextView(this).apply {
                text = "ASTRAL FORGE: DEPTHBOUND"
                textSize = 10f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                letterSpacing = 0.10f
                setPadding(
                    0,
                    AuthUi.dp(this@LegalActivity, 3),
                    0,
                    AuthUi.dp(this@LegalActivity, 4)
                )
            }
        )

        content.addView(
            TextView(this).apply {
                text = "Effective October 7, 2026 • Development Build"
                textSize = 9.5f
                setTextColor(Color.rgb(190, 177, 217))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    0,
                    0,
                    AuthUi.dp(this@LegalActivity, 12)
                )
            }
        )

        val panel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                AuthUi.dp(this@LegalActivity, 16),
                AuthUi.dp(this@LegalActivity, 14),
                AuthUi.dp(this@LegalActivity, 16),
                AuthUi.dp(this@LegalActivity, 14)
            )
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    Color.argb(240, 31, 14, 68),
                    Color.argb(245, 7, 8, 29)
                )
            ).apply {
                cornerRadius = AuthUi.dp(this@LegalActivity, 15).toFloat()
                setStroke(
                    AuthUi.dp(this@LegalActivity, 1),
                    Color.rgb(218, 174, 94)
                )
            }
        }

        if (isPrivacy) {
            addPrivacySections(panel)
        } else {
            addTermsSections(panel)
        }

        content.addView(
            panel,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        content.addView(
            TextView(this).apply {
                text = "Use Android Back to return to About."
                textSize = 9f
                setTextColor(Color.rgb(160, 148, 190))
                gravity = Gravity.CENTER
                setPadding(
                    0,
                    AuthUi.dp(this@LegalActivity, 9),
                    0,
                    0
                )
            }
        )

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

        root.setOnApplyWindowInsetsListener { _, insets: WindowInsets ->
            content.setPadding(
                AuthUi.dp(this@LegalActivity, 24),
                insets.systemWindowInsetTop + AuthUi.dp(this@LegalActivity, 18),
                AuthUi.dp(this@LegalActivity, 24),
                insets.systemWindowInsetBottom + AuthUi.dp(this@LegalActivity, 26)
            )
            insets
        }
        root.requestApplyInsets()

        setContentView(root)
    }

    private fun addPrivacySections(panel: LinearLayout) {
        panel.addView(section(
            "OVERVIEW",
            "This Privacy Policy describes how Astral Forge: Depthbound handles " +
                "information in the current Android development build."
        ))

        panel.addView(section(
            "ACCOUNT INFORMATION",
            "Registered accounts use Firebase Authentication. Account creation may " +
                "process your email address, username or display name, Firebase user ID, " +
                "and account creation time. Google sign-in may also provide account " +
                "information permitted by your Google sign-in choice. Guest accounts use " +
                "an anonymous Firebase account."
        ))

        panel.addView(section(
            "GAME & DEVICE DATA",
            "The app stores player name, progression values, currencies, energy, stats, " +
                "class and title information, game settings, audio preferences, visual " +
                "preferences, notification preference, and remember-me preference on the device."
        ))

        panel.addView(section(
            "PASSWORDS & AUTHENTICATION",
            "Email/password and Google authentication are handled through Firebase " +
                "Authentication and Google sign-in services. Astral Forge: Depthbound " +
                "does not intentionally store your plain-text password in its local game data."
        ))

        panel.addView(section(
            "HOW INFORMATION IS USED",
            "Information is used to sign you in, identify your player account, display " +
                "your player name, operate account and progression features, remember " +
                "settings, troubleshoot problems, and support continued game development."
        ))

        panel.addView(section(
            "THIRD-PARTY SERVICES",
            "The current build uses Google and Firebase services for authentication and " +
                "account-related cloud features. Links to GitHub may open the project " +
                "repository, update log, or bug tracker in your browser. Those services " +
                "operate under their own privacy terms."
        ))

        panel.addView(section(
            "LOCAL STORAGE & RETENTION",
            "Progression, settings, player-name data, and certain sign-in preferences are " +
                "stored locally using Android app storage. Cloud account information may " +
                "remain with Firebase until it is deleted or otherwise removed under the " +
                "app's account-management process or the relevant provider's controls."
        ))

        panel.addView(section(
            "DATA SHARING",
            "The game does not intentionally sell personal information. Information may " +
                "be processed by service providers required to operate authentication, " +
                "cloud account features, or linked services."
        ))

        panel.addView(section(
            "SECURITY",
            "Reasonable platform and provider safeguards are used, but no digital system " +
                "can guarantee absolute security. Development builds may change as security " +
                "and account systems are improved."
        ))

        panel.addView(section(
            "CHILDREN",
            "This development build is not specifically directed to children under 13. " +
                "Public-release age requirements and parental-consent language will be " +
                "finalized before distribution where required."
        ))

        panel.addView(section(
            "YOUR CHOICES",
            "You can change many gameplay and device preferences in Settings and can sign " +
                "out from the account menu. A complete in-app account-deletion workflow is " +
                "not yet available in this development build and should be completed before public release."
        ))

        panel.addView(section(
            "CHANGES & CONTACT",
            "This policy may be updated as game systems change. Questions or privacy " +
                "concerns can currently be raised through the Astral Forge: Depthbound " +
                "GitHub issue tracker while the project is in development."
        ))
    }

    private fun addTermsSections(panel: LinearLayout) {
        panel.addView(section(
            "ACCEPTANCE",
            "By using this development build of Astral Forge: Depthbound, you agree to " +
                "these terms as they apply to the current testing and development version."
        ))

        panel.addView(section(
            "DEVELOPMENT-BUILD STATUS",
            "Depthbound is actively being developed. Features, art, balance, progression, " +
                "availability, data formats, interfaces, and online services may change, " +
                "be reset, become unavailable, or be removed before public release."
        ))

        panel.addView(section(
            "ACCOUNTS",
            "You are responsible for activity performed through your account and for " +
                "keeping access to your sign-in methods secure. Do not attempt to access " +
                "another player's account or bypass account protections."
        ))

        panel.addView(section(
            "ACCEPTABLE USE",
            "Do not use the game or its services to exploit vulnerabilities, interfere " +
                "with service operation, harass other users, distribute malicious code, " +
                "or obtain unauthorized access to systems or data. Good-faith bug reports " +
                "through the project issue tracker are welcome."
        ))

        panel.addView(section(
            "GAME PROGRESS & VIRTUAL ITEMS",
            "Levels, currencies, shards, energy, gear, heroes, summons, stats, and other " +
                "virtual game values are gameplay features. Unless explicitly stated " +
                "otherwise in a future release, they have no guaranteed real-world cash value."
        ))

        panel.addView(section(
            "INTELLECTUAL PROPERTY",
            "Astral Forge: Depthbound, its original game systems, interface presentation, " +
                "project-specific artwork, writing, and other original project materials " +
                "are reserved to their respective rights holders. Third-party tools, " +
                "services, libraries, and assets remain subject to their own licenses."
        ))

        panel.addView(section(
            "THIRD-PARTY SERVICES",
            "The game may rely on or link to services such as Google, Firebase, and GitHub. " +
                "Use of those services may also be governed by their own terms and policies."
        ))

        panel.addView(section(
            "AVAILABILITY",
            "There is no guarantee that this development build, its servers, account " +
                "services, test data, or individual features will remain continuously available."
        ))

        panel.addView(section(
            "DISCLAIMER",
            "This development build is provided for testing and development purposes on an " +
                "as-available basis. Bugs, incomplete features, crashes, balance changes, " +
                "or data resets may occur."
        ))

        panel.addView(section(
            "LIMITATION OF LIABILITY",
            "To the extent permitted by applicable law, the project should not be treated " +
                "as guaranteeing uninterrupted operation, preservation of test progress, " +
                "or fitness of unfinished development features for a particular purpose."
        ))

        panel.addView(section(
            "TERMINATION",
            "Access may be suspended or ended for misuse, security reasons, discontinued " +
                "testing, or changes to the project. You may stop using the development " +
                "build at any time."
        ))

        panel.addView(section(
            "LEGAL DETAILS BEFORE RELEASE",
            "Final public-release terms should include finalized governing-law, dispute, " +
                "contact, store-platform, refund, purchase, subscription, age-rating, " +
                "license, and jurisdiction provisions if those features or requirements apply."
        ))

        panel.addView(section(
            "CHANGES",
            "These terms may be revised as Depthbound develops. The effective date shown " +
                "at the top identifies the version currently presented in the app."
        ))
    }

    private fun section(title: String, body: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                0,
                AuthUi.dp(this@LegalActivity, 4),
                0,
                AuthUi.dp(this@LegalActivity, 9)
            )

            addView(TextView(this@LegalActivity).apply {
                text = title
                textSize = 10f
                setTextColor(Color.rgb(230, 196, 125))
                typeface = Typeface.DEFAULT_BOLD
                letterSpacing = 0.08f
                setPadding(
                    0,
                    0,
                    0,
                    AuthUi.dp(this@LegalActivity, 4)
                )
            })

            addView(TextView(this@LegalActivity).apply {
                text = body
                textSize = 10.2f
                setTextColor(Color.rgb(211, 201, 234))
                lineSpacingMultiplier = 1.08f
            })
        }
    }
}
