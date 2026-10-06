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
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialManagerCallback
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class MainActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        if (!authPrefs.getBoolean("remember_me", true) && auth.currentUser != null) {
            auth.signOut()
        }

        if (authPrefs.getBoolean("remember_me", true) && auth.currentUser != null) {
            openHub()
            return
        }

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
            isChecked = authPrefs.getBoolean("remember_me", true)
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
                    else -> {
                        isEnabled = false
                        text = getString(R.string.signing_in)

                        auth.signInWithEmailAndPassword(emailValue, passwordValue)
                            .addOnSuccessListener { result ->
                                val user = result.user
                                if (user == null) {
                                    restoreSignInButton(this)
                                    Toast.makeText(
                                        this@MainActivity,
                                        getString(R.string.sign_in_failed),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    return@addOnSuccessListener
                                }

                                authPrefs.edit()
                                    .putBoolean("remember_me", rememberMe.isChecked)
                                    .apply()

                                firestore.collection("players")
                                    .document(user.uid)
                                    .set(
                                        mapOf("lastLoginAt" to FieldValue.serverTimestamp()),
                                        SetOptions.merge()
                                    )
                                    .addOnCompleteListener {
                                        restoreSignInButton(this)
                                        openHub()
                                    }
                            }
                            .addOnFailureListener { error ->
                                restoreSignInButton(this)
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(
                                        R.string.sign_in_failed_with_reason,
                                        error.localizedMessage ?: getString(R.string.unknown_error)
                                    ),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
                }
            }
        }

        val googleSignIn = Button(this).apply {
            text = getString(R.string.sign_in_with_google)
            setOnClickListener {
                signInWithGoogle(this)
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
                val emailValue = email.text.toString().trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                    email.error = getString(R.string.error_email_for_reset)
                    email.requestFocus()
                    return@setOnClickListener
                }

                isEnabled = false
                auth.sendPasswordResetEmail(emailValue)
                    .addOnSuccessListener {
                        isEnabled = true
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.password_reset_sent),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { error ->
                        isEnabled = true
                        Toast.makeText(
                            this@MainActivity,
                            getString(
                                R.string.password_reset_failed,
                                error.localizedMessage ?: getString(R.string.unknown_error)
                            ),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
        }

        val guest = Button(this).apply {
            text = getString(R.string.continue_as_guest)
            setOnClickListener {
                continueAsGuest(this)
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
        root.addView(googleSignIn)
        spacer()
        root.addView(createAccount)
        root.addView(forgotPassword)
        root.addView(guest)

        scroll.addView(root)
        setContentView(scroll)
    }

    private fun signInWithGoogle(button: Button) {
        button.isEnabled = false
        button.text = getString(R.string.signing_in_with_google)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)
        credentialManager.getCredentialAsync(
            this,
            request,
            null,
            mainExecutor,
            object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                override fun onResult(result: GetCredentialResponse) {
                    val credential = result.credential
                    if (
                        credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        try {
                            val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            firebaseAuthWithGoogle(googleCredential.idToken, button)
                        } catch (error: Exception) {
                            restoreGoogleButton(button)
                            Toast.makeText(
                                this@MainActivity,
                                getString(
                                    R.string.google_sign_in_failed_with_reason,
                                    error.localizedMessage ?: getString(R.string.unknown_error)
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        restoreGoogleButton(button)
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.google_sign_in_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(error: GetCredentialException) {
                    restoreGoogleButton(button)
                    Toast.makeText(
                        this@MainActivity,
                        getString(
                            R.string.google_sign_in_failed_with_reason,
                            error.localizedMessage ?: getString(R.string.unknown_error)
                        ),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun firebaseAuthWithGoogle(idToken: String, button: Button) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        val existingUser = auth.currentUser

        val authTask =
            if (existingUser != null && existingUser.isAnonymous) {
                existingUser.linkWithCredential(firebaseCredential)
            } else {
                auth.signInWithCredential(firebaseCredential)
            }

        authTask
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    restoreGoogleButton(button)
                    Toast.makeText(
                        this,
                        getString(R.string.google_sign_in_failed),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                val profile = mutableMapOf<String, Any>(
                    "uid" to user.uid,
                    "displayName" to (user.displayName ?: "Player"),
                    "email" to (user.email ?: ""),
                    "accountType" to "google",
                    "lastLoginAt" to FieldValue.serverTimestamp(),
                    "appVersion" to "0.0.0.1"
                )

                if (result.additionalUserInfo?.isNewUser == true) {
                    profile["createdAt"] = FieldValue.serverTimestamp()
                }

                firestore.collection("players")
                    .document(user.uid)
                    .set(profile, SetOptions.merge())
                    .addOnCompleteListener {
                        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("remember_me", true)
                            .apply()

                        restoreGoogleButton(button)
                        openHub()
                    }
            }
            .addOnFailureListener { error ->
                restoreGoogleButton(button)
                Toast.makeText(
                    this,
                    getString(
                        R.string.google_sign_in_failed_with_reason,
                        error.localizedMessage ?: getString(R.string.unknown_error)
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun restoreGoogleButton(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.sign_in_with_google)
    }

    private fun continueAsGuest(button: Button) {
        button.isEnabled = false
        button.text = getString(R.string.starting_guest)

        val authPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        authPrefs.edit()
            .putBoolean("remember_me", true)
            .apply()

        val existingUser = auth.currentUser
        if (existingUser != null && existingUser.isAnonymous) {
            saveGuestProfile(existingUser.uid, button)
            return
        }

        if (existingUser != null) {
            auth.signOut()
        }

        auth.signInAnonymously()
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    restoreGuestButton(button)
                    Toast.makeText(
                        this,
                        getString(R.string.guest_sign_in_failed),
                        Toast.LENGTH_LONG
                    ).show()
                    return@addOnSuccessListener
                }

                saveGuestProfile(user.uid, button)
            }
            .addOnFailureListener { error ->
                restoreGuestButton(button)
                Toast.makeText(
                    this,
                    getString(
                        R.string.guest_sign_in_failed_with_reason,
                        error.localizedMessage ?: getString(R.string.unknown_error)
                    ),
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun saveGuestProfile(uid: String, button: Button) {
        val profile = mapOf(
            "uid" to uid,
            "displayName" to "Guest",
            "accountType" to "guest",
            "createdAt" to FieldValue.serverTimestamp(),
            "lastLoginAt" to FieldValue.serverTimestamp(),
            "appVersion" to "0.0.0.1"
        )

        firestore.collection("players")
            .document(uid)
            .set(profile, SetOptions.merge())
            .addOnSuccessListener {
                restoreGuestButton(button)
                openHub()
            }
            .addOnFailureListener { error ->
                restoreGuestButton(button)
                Toast.makeText(
                    this,
                    getString(
                        R.string.guest_profile_save_failed,
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

    private fun restoreGuestButton(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.continue_as_guest)
    }

    private fun restoreSignInButton(button: Button) {
        button.isEnabled = true
        button.text = getString(R.string.sign_in_button)
    }
}
