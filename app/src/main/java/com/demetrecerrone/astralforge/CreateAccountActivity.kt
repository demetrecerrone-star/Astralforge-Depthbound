package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fun dp(value: Int) = AuthUi.dp(this, value)

        val scroll = ScrollView(this).apply {
            background = AuthUi.screenBackground()
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(28), dp(22), dp(36))
        }

        root.addView(AuthUi.brandTitle(this))
        root.addView(AuthUi.brandSubtitle(this))
        root.addView(AuthUi.ornament(this))

        val title = TextView(this).apply {
            text = getString(R.string.create_account_title).uppercase()
            setTextColor(AuthUi.textPrimary)
            textSize = 22f
            gravity = Gravity.CENTER
            letterSpacing = 0.08f
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.create_account_subtitle)
            setTextColor(AuthUi.textSecondary)
            textSize = 13f
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(6), dp(8), dp(20))
        }

        root.addView(title)
        root.addView(subtitle)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = AuthUi.panelBackground(this@CreateAccountActivity)
            setPadding(dp(18), dp(20), dp(18), dp(20))
        }

        root.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        fun addToCard(view: View, topMargin: Int = 0) {
            card.addView(
                view,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { this.topMargin = dp(topMargin) }
            )
        }

        fun field(hintText: String, inputTypeValue: Int): EditText =
            EditText(this).apply {
                hint = hintText
                inputType = inputTypeValue
                AuthUi.styleField(this@CreateAccountActivity, this)
            }

        val displayName = field(
            getString(R.string.display_name_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        )

        val email = field(
            getString(R.string.email_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val password = field(
            getString(R.string.password_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        val confirmPassword = field(
            getString(R.string.confirm_password_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        addToCard(displayName)
        addToCard(email, 11)
        addToCard(password, 11)
        addToCard(confirmPassword, 11)

        val terms = CheckBox(this).apply {
            val prefix = getString(R.string.accept_terms_prefix)
            val termsLabel = getString(R.string.terms_of_service)
            val connector = getString(R.string.accept_terms_connector)
            val privacyLabel = getString(R.string.privacy_policy)
            val fullText = prefix + termsLabel + connector + privacyLabel + "."
            val linkedText = SpannableString(fullText)

            val termsStart = prefix.length
            val termsEnd = termsStart + termsLabel.length
            linkedText.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        startActivity(
                            Intent(this@CreateAccountActivity, LegalDocumentActivity::class.java)
                                .putExtra(
                                    LegalDocumentActivity.EXTRA_DOCUMENT,
                                    LegalDocumentActivity.DOCUMENT_TERMS
                                )
                        )
                    }
                },
                termsStart,
                termsEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            val privacyStart = termsEnd + connector.length
            val privacyEnd = privacyStart + privacyLabel.length
            linkedText.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        startActivity(
                            Intent(this@CreateAccountActivity, LegalDocumentActivity::class.java)
                                .putExtra(
                                    LegalDocumentActivity.EXTRA_DOCUMENT,
                                    LegalDocumentActivity.DOCUMENT_PRIVACY
                                )
                        )
                    }
                },
                privacyStart,
                privacyEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            text = linkedText
            movementMethod = LinkMovementMethod.getInstance()
            highlightColor = Color.TRANSPARENT
            setLinkTextColor(AuthUi.violetSoft)
            setTextColor(AuthUi.textPrimary)
            textSize = 12f
            buttonTintList = ColorStateList.valueOf(AuthUi.violet)
        }
        addToCard(terms, 10)

        val updates = CheckBox(this).apply {
            text = getString(R.string.receive_updates)
            setTextColor(AuthUi.textSecondary)
            textSize = 12f
            buttonTintList = ColorStateList.valueOf(AuthUi.violet)
        }
        addToCard(updates, 2)

        val createButton = Button(this).apply {
            text = getString(R.string.create_account_button)
            AuthUi.stylePrimary(this@CreateAccountActivity, this)
            setOnClickListener {
                val nameValue = displayName.text.toString().trim()
                val emailValue = email.text.toString().trim()
                val passwordValue = password.text.toString()
                val confirmValue = confirmPassword.text.toString()

                when {
                    nameValue.length < 3 -> {
                        displayName.error = getString(R.string.error_display_name)
                        displayName.requestFocus()
                    }
                    !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                        email.error = getString(R.string.error_email)
                        email.requestFocus()
                    }
                    passwordValue.length < 8 -> {
                        password.error = getString(R.string.error_password_length)
                        password.requestFocus()
                    }
                    passwordValue != confirmValue -> {
                        confirmPassword.error = getString(R.string.error_password_match)
                        confirmPassword.requestFocus()
                    }
                    !terms.isChecked -> {
                        Toast.makeText(
                            this@CreateAccountActivity,
                            getString(R.string.error_terms),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> createAccount(
                        nameValue = nameValue,
                        emailValue = emailValue,
                        passwordValue = passwordValue,
                        receiveUpdates = updates.isChecked,
                        createButton = this
                    )
                }
            }
        }
        addToCard(createButton, 12)

        val backToSignIn = Button(this).apply {
            text = getString(R.string.back_to_sign_in)
            AuthUi.styleSecondary(this@CreateAccountActivity, this)
            setOnClickListener { finish() }
        }
        addToCard(backToSignIn, 10)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun createAccount(
        nameValue: String,
        emailValue: String,
        passwordValue: String,
        receiveUpdates: Boolean,
        createButton: Button
    ) {
        createButton.isEnabled = false
        createButton.text = getString(R.string.creating_account)

        auth.createUserWithEmailAndPassword(emailValue, passwordValue)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    restoreCreateButton(createButton)
                    Toast.makeText(
                        this,
                        getString(R.string.account_create_failed),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val profile = hashMapOf(
                    "uid" to user.uid,
                    "displayName" to nameValue,
                    "email" to emailValue,
                    "receiveUpdates" to receiveUpdates,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "lastLoginAt" to FieldValue.serverTimestamp(),
                    "accountType" to "email",
                    "appVersion" to "0.0.0.1"
                )

                firestore.collection("players")
                    .document(user.uid)
                    .set(profile)
                    .addOnSuccessListener {
                        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("remember_me", true)
                            .apply()
                        openHub()
                    }
                    .addOnFailureListener { error ->
                        restoreCreateButton(createButton)
                        Toast.makeText(
                            this,
                            getString(
                                R.string.profile_save_failed,
                                error.localizedMessage ?: getString(R.string.unknown_error)
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { error ->
                restoreCreateButton(createButton)
                Toast.makeText(
                    this,
                    getString(
                        R.string.account_create_failed_with_reason,
                        error.localizedMessage ?: getString(R.string.unknown_error)
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun openHub() {
        startActivity(
            Intent(this, MainHubActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    private fun restoreCreateButton(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.create_account_button)
    }
}
