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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class CreateAccountActivity : Activity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usernameField: android.widget.EditText
    private lateinit var emailField: android.widget.EditText
    private lateinit var passwordField: android.widget.EditText
    private lateinit var confirmField: android.widget.EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthUi.setupWindow(this)
        auth = FirebaseAuth.getInstance()

        val content = AuthUi.createScreen(this, 0.38f)

        content.addView(AuthUi.heading(this, getString(R.string.create_account_heading), 17f))
        content.addView(AuthUi.subtitle(this, getString(R.string.create_account_subtitle)))

        usernameField = AuthUi.field(this, getString(R.string.username), R.drawable.ic_user)
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
        confirmField = AuthUi.field(
            this,
            getString(R.string.confirm_password),
            R.drawable.ic_lock,
            isPassword = true,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        AuthUi.twoFieldRow(content, usernameField, emailField, topMargin = true)
        AuthUi.twoFieldRow(content, passwordField, confirmField, topMargin = true)

        content.addView(
            AuthUi.assetButton(
                this,
                R.drawable.file_00000000a694822fabb704680dcacbc2,
                "Create Account",
                58
            ) { createAccount() }
        )

        content.addView(AuthUi.divider(this))

        content.addView(
            AuthUi.assetButton(
                this,
                R.drawable.file_0000000004a881f6b93ff7dc066c5f95,
                "Continue with Google",
                46
            ) { startGoogleSignIn() }
        )

        content.addView(
            AuthUi.linkRow(
                this,
                getString(R.string.already_account),
                getString(R.string.sign_in_link)
            ) { finish() },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = AuthUi.dp(this@CreateAccountActivity, 8)
            }
        )
    }

    private fun createAccount() {
        val username = usernameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()
        val confirm = confirmField.text.toString()

        when {
            username.length < 3 -> toast("Username must be at least 3 characters.")
            email.isBlank() -> toast("Enter your email.")
            password.length < 6 -> toast("Password must be at least 6 characters.")
            password != confirm -> toast("Passwords do not match.")
            else -> {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val user = auth.currentUser ?: return@addOnSuccessListener
                        val update = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()
                        user.updateProfile(update)

                        FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(user.uid)
                            .set(
                                mapOf(
                                    "username" to username,
                                    "email" to email,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                            )
                        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                            .edit().putBoolean("remember_me", true).apply()
                        openHome()
                    }
                    .addOnFailureListener { toast(it.localizedMessage ?: "Account creation failed.") }
            }
        }
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
        auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null))
            .addOnSuccessListener {
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit().putBoolean("remember_me", true).apply()
                openHome()
            }
            .addOnFailureListener { toast(it.localizedMessage ?: "Google authentication failed.") }
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finishAffinity()
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    companion object {
        private const val GOOGLE_SIGN_IN = 9002
    }
}
