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
    private lateinit var remember: android.widget.ToggleButton

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


        val root = AuthUi.embeddedCanvas(this, R.drawable.auth_login_embedded)
        emailField = AuthUi.overlayField(this, getString(R.string.email),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordField = AuthUi.overlayField(this, getString(R.string.password),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, true)
        AuthUi.place(root, emailField, 651, 383, 475, 72)
        AuthUi.place(root, passwordField, 651, 480, 450, 71)
        AuthUi.overlayEye(root, passwordField, 1132, 479)
        remember = AuthUi.separateRemember(this, shouldRemember)
        AuthUi.place(root, remember, 557, 563, 46, 46)
        AuthUi.overlayText(root, getString(R.string.remember_me), 615, 558, 170, 54, 12.5f)
        AuthUi.overlayLink(root, getString(R.string.forgot_password), 960, 559, 219, 50) {
            sendPasswordReset()
        }
        AuthUi.overlayButton(root, "SIGN IN", 618, 610, 438, 86) {
            signInWithEmail()
        }
        AuthUi.overlayTap(root, "Continue with Google", 535, 741, 287, 95) {
            startGoogleSignIn()
        }
        AuthUi.overlayTap(root, "Continue as Guest", 847, 741, 287, 95) {
            signInAsGuest()
        }
        AuthUi.overlayLink(root, "Create Account", 738, 844, 212, 64) {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        DabskyIntroOverlay.show(this)
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
