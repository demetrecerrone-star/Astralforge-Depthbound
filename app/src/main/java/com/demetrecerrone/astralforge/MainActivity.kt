package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
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

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density
        fun dp(value: Int) = (value * density).toInt()

        val scroll = ScrollView(this).apply {
            setBackgroundColor(Color.rgb(8, 10, 28))
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(48), dp(24), dp(36))
        }

        val title = TextView(this).apply {
            text = getString(R.string.app_name)
            setTextColor(Color.rgb(236, 230, 255))
            textSize = 30f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = getString(R.string.login_subtitle)
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

        val email = field(
            getString(R.string.email_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        val password = field(
            getString(R.string.password_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        val rememberMe = CheckBox(this).apply {
            text = getString(R.string.remember_me)
            setTextColor(Color.rgb(220, 215, 240))
        }

        val signIn = Button(this).apply {
            text = getString(R.string.sign_in_button)
            setOnClickListener {
                val emailValue = email.text.toString().trim()
                val passwordValue = password.text.toString()

                when {
                    !Patterns.EMAIL_ADDRESS.matcher(emailValue).matches() -> {
                        email.error = getString(R.string.error_email)
                        email.requestFocus()
                    }
                    passwordValue.length < 8 -> {
                        password.error = getString(R.string.error_password_length)
                        password.requestFocus()
                    }
                    else -> Toast.makeText(
                        this@MainActivity,
                        getString(R.string.sign_in_placeholder),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val createAccount = Button(this).apply {
            text = getString(R.string.create_account_button)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, CreateAccountActivity::class.java))
            }
        }

        val forgotPassword = Button(this).apply {
            text = getString(R.string.forgot_password)
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.forgot_password_placeholder),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val guest = Button(this).apply {
            text = getString(R.string.continue_as_guest)
            setOnClickListener {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.guest_flow_placeholder),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        fun spacer(height: Int = 10) {
            root.addView(
                TextView(this),
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(height)
                )
            )
        }

        root.addView(title)
        root.addView(subtitle)

        listOf(email, password).forEach {
            root.addView(
                it,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )
            spacer()
        }

        root.addView(rememberMe)
        spacer()
        root.addView(signIn)
        spacer()
        root.addView(createAccount)
        root.addView(forgotPassword)
        root.addView(guest)

        scroll.addView(root)
        setContentView(scroll)
    }
}
