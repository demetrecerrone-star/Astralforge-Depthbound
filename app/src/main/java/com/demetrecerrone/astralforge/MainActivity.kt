package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.LinearLayout
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : Activity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var emailField: android.widget.EditText
    private lateinit var passwordField: android.widget.EditText
    private lateinit var remember: android.widget.CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)
        auth = FirebaseAuth.getInstance()

        val prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val shouldRemember = prefs.getBoolean("remember_me", false)
        if (auth.currentUser != null && shouldRemember) {
            openHome()
            return
        } else if (auth.currentUser != null && !auth.currentUser!!.isAnonymous) {
            auth.signOut()
        }

        val content = AuthUi.createLandscapeScreen(
            this,
            "WELCOME BACK",
            "Sign in to continue your descent."
        )

        emailField = AuthUi.field(
            this,
            getString(R.string.email),
            R.drawable.ic_mail,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        passwordField = AuthUi.field(
            this,
            getString(R.string.password),
            R.drawable.ic_lock,
            isPassword = true,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )
        AuthUi.addField(content, emailField, 4)
        AuthUi.addField(content, passwordField, 8)

        val options = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
        }
        remember = AuthUi.rememberBox(this).apply { isChecked = shouldRemember }
        options.addView(remember, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        options.addView(
            AuthUi.smallLink(this@MainActivity, getString(R.string.forgot_password)) { sendPasswordReset() },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        )
        content.addView(options, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        content.addView(
            AuthUi.assetButton(
                this,
                R.drawable.file_00000000d90c81f590bfb22d91dd428d,
                "Sign In",
                72
            ) { signInWithEmail() }
        )

        content.addView(AuthUi.divider(this))

        AuthUi.addButtonPair(
            content,
            AuthUi.assetButton(
                this,
                R.drawable.file_0000000004a881f6b93ff7dc066c5f95,
                "Continue with Google",
                56
            ) { startGoogleSignIn() },
            AuthUi.assetButton(
                this,
                R.drawable.file_000000000dd481f6a8aebb5476e7346d,
                "Continue as Guest",
                56
            ) { signInAsGuest() },
            heightDp = 56,
            topMarginDp = 2
        )

        val link = AuthUi.linkRow(
            this,
            getString(R.string.no_account),
            getString(R.string.create_account_link)
        ) {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }
        content.addView(
            link,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = AuthUi.dp(this@MainActivity, 6)
            }
        )
    }

    private fun signInWithEmail() {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()
        if (email.isBlank() || password.isBlank()) {
            toast("Enter your email and password.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember_me", remember.isChecked).apply()
                openHome()
            }
            .addOnFailureListener { toast(it.localizedMessage ?: "Sign in failed.") }
    }

    private fun sendPasswordReset() {
        val email = emailField.text.toString().trim()
        if (email.isBlank()) {
            toast("Enter your email first.")
            return
        }
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener { toast("Password reset email sent.") }
            .addOnFailureListener { toast(it.localizedMessage ?: "Could not send reset email.") }
    }

    private fun signInAsGuest() {
        auth.signInAnonymously()
            .addOnSuccessListener {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember_me", false).apply()
                openHome()
            }
            .addOnFailureListener { toast(it.localizedMessage ?: "Guest sign in failed.") }
    }

    private fun startGoogleSignIn() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(this, options)
        client.signOut().addOnCompleteListener {
            @Suppress("DEPRECATION")
            startActivityForResult(client.signInIntent, GOOGLE_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != GOOGLE_SIGN_IN) return

        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)
            firebaseWithGoogle(account)
        } catch (e: ApiException) {
            toast("Google sign in failed: " + e.statusCode)
        }
    }

    private fun firebaseWithGoogle(account: GoogleSignInAccount) {
        val token = account.idToken
        if (token.isNullOrBlank()) {
            toast("Google sign in did not return an ID token.")
            return
        }
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember_me", remember.isChecked).apply()
                openHome()
            }
            .addOnFailureListener { toast(it.localizedMessage ?: "Google authentication failed.") }
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object {
        private const val GOOGLE_SIGN_IN = 9001
    }
}
