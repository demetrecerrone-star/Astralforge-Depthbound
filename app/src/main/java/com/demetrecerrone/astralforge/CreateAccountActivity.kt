package com.demetrecerrone.astralforge

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.Gravity
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

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(36), dp(24), dp(36))
        }

        val title = TextView(this).apply {
            text = getString(R.string.create_account_title)
            setTextColor(Color.rgb(236, 230, 255))
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.create_account_subtitle)
            setTextColor(Color.rgb(173, 163, 214))
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(28))
        }

        fun field(hintText: String, inputTypeValue: Int): EditText =
            EditText(this).apply {
                hint = hintText
                inputType = inputTypeValue
                setTextColor(Color.WHITE)
                setHintTextColor(Color.rgb(125, 117, 160))
                setBackgroundColor(Color.rgb(20, 22, 48))
                setPadding(dp(14), dp(12), dp(14), dp(12))
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

        val terms = CheckBox(this).apply {
            text = getString(R.string.accept_terms)
            setTextColor(Color.rgb(220, 215, 240))
        }

        val updates = CheckBox(this).apply {
            text = getString(R.string.receive_updates)
            setTextColor(Color.rgb(190, 184, 216))
        }

        val createButton = Button(this).apply {
            text = getString(R.string.create_account_button)
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

        val backToSignIn = Button(this).apply {
            text = getString(R.string.back_to_sign_in)
            setOnClickListener { finish() }
        }

        val guest = Button(this).apply {
            text = getString(R.string.continue_as_guest)
            setOnClickListener {
                Toast.makeText(
                    this@CreateAccountActivity,
                    getString(R.string.guest_flow_placeholder),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun addSpacing() {
            root.addView(
                TextView(this),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(10)
                )
            )
        }

        root.addView(title)
        root.addView(subtitle)

        listOf(displayName, email, password, confirmPassword).forEach {
            root.addView(
                it,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            addSpacing()
        }

        root.addView(terms)
        root.addView(updates)
        addSpacing()
        root.addView(createButton)
        addSpacing()
        root.addView(backToSignIn)
        root.addView(guest)

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
                        Toast.makeText(
                            this,
                            getString(R.string.account_created_success),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
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

    private fun restoreCreateButton(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.create_account_button)
    }
}
