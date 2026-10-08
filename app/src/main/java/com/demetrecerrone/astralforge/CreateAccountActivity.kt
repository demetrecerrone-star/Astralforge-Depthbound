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


        val root = AuthUi.embeddedCanvas(this, R.drawable.auth_create_embedded)
        usernameField = AuthUi.overlayField(this, getString(R.string.username),
            InputType.TYPE_CLASS_TEXT)
        emailField = AuthUi.overlayField(this, getString(R.string.email),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        passwordField = AuthUi.overlayField(this, getString(R.string.password),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, true)
        confirmField = AuthUi.overlayField(this, getString(R.string.confirm_password),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD, true)
        AuthUi.place(root, usernameField, 524, 364, 216, 77)
        AuthUi.place(root, emailField, 943, 364, 223, 77)
        AuthUi.place(root, passwordField, 524, 469, 176, 77)
        AuthUi.place(root, confirmField, 943, 469, 176, 77)
        AuthUi.overlayEye(root, passwordField, 704, 469)
        AuthUi.overlayEye(root, confirmField, 1130, 469)
        AuthUi.overlayButton(root, "CREATE ACCOUNT", 587, 560, 499, 94) {
            createAccount()
        }
        AuthUi.overlayTap(root, "Continue with Google", 652, 700, 369, 99) {
            startGoogleSignIn()
        }
        AuthUi.overlayLink(root, "Already have an account? Sign In", 640, 834, 395, 64) {
            finish()
        }
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
