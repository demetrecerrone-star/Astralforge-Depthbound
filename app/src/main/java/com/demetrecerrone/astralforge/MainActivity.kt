package com.demetrecerrone.astralforge

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.Gravity
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
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

        val exactArtwork = android.graphics.BitmapFactory.decodeResource(
            resources,
            R.drawable.login_exact
        )
        showExactLogin(exactArtwork, authPrefs)
        return

        fun dp(value: Int) = AuthUi.dp(this, value)

        val screen = FrameLayout(this).apply {
            background = AuthUi.screenBackground()
        }

        val fantasyBackground = AuthFantasyBackgroundView(this)

        val scroll = ScrollView(this).apply {
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            isFillViewport = true
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(22), dp(24), dp(22), dp(34))
        }

        root.addView(AuthUi.brandTitle(this))
        root.addView(AuthUi.brandSubtitle(this))
        root.addView(AuthUi.ornament(this))

        val tagline = TextView(this).apply {
            text = "Enter the Depths. Forge what survives."
            setTextColor(ColorStateList.valueOf(AuthUi.textPrimary).defaultColor)
            textSize = 16f
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC)
            setPadding(0, dp(2), 0, dp(18))
        }
        root.addView(tagline)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            setPadding(dp(2), dp(2), dp(2), dp(4))
        }
        root.addView(
            card,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        fun addToCard(view: android.view.View, topMargin: Int = 0) {
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
                AuthUi.styleField(this@MainActivity, this)
            }

        val email = field(
            getString(R.string.email_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        val password = field(
            getString(R.string.password_hint),
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        )

        addToCard(email)
        addToCard(password, 12)

        val optionsRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val rememberMe = CheckBox(this).apply {
            text = getString(R.string.remember_me)
            setTextColor(AuthUi.textPrimary)
            textSize = 13f
            buttonTintList = ColorStateList.valueOf(AuthUi.violet)
            isChecked = authPrefs.getBoolean("remember_me", true)
        }

        val forgotPassword = Button(this).apply {
            text = getString(R.string.forgot_password)
            AuthUi.styleSecondary(this@MainActivity, this)
        }

        optionsRow.addView(
            rememberMe,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        addToCard(optionsRow, 4)

        val signIn = Button(this).apply {
            text = getString(R.string.sign_in_button)
            AuthUi.stylePrimary(this@MainActivity, this)
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
        addToCard(signIn, 10)

        forgotPassword.setOnClickListener {
            val emailValue = email.text.toString().trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                email.error = getString(R.string.error_email_for_reset)
                email.requestFocus()
                return@setOnClickListener
            }

            forgotPassword.isEnabled = false
            auth.sendPasswordResetEmail(emailValue)
                .addOnSuccessListener {
                    forgotPassword.isEnabled = true
                    Toast.makeText(
                        this@MainActivity,
                        getString(R.string.password_reset_sent),
                        Toast.LENGTH_LONG
                    ).show()
                }
                .addOnFailureListener { error ->
                    forgotPassword.isEnabled = true
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

        val googleSignIn = Button(this).apply {
            text = "G   " + getString(R.string.sign_in_with_google)
            AuthUi.styleGoogle(this@MainActivity, this)
            setOnClickListener { signInWithGoogle(this) }
        }
        addToCard(googleSignIn, 14)

        val createAccount = Button(this).apply {
            text = getString(R.string.create_account_button)
            AuthUi.styleSecondary(this@MainActivity, this)
            setOnClickListener {
                startActivity(Intent(this@MainActivity, CreateAccountActivity::class.java))
            }
        }
        addToCard(createAccount, 12)

        addToCard(forgotPassword, 12)

        val guest = Button(this).apply {
            text = "♟   " + getString(R.string.continue_as_guest)
            AuthUi.styleGuest(this@MainActivity, this)
            setOnClickListener { continueAsGuest(this) }
        }
        addToCard(guest, 12)

        scroll.addView(root)
        screen.addView(
            fantasyBackground,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        screen.addView(
            scroll,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(screen)
    }

    private fun showExactLogin(
        artwork: android.graphics.Bitmap,
        authPrefs: android.content.SharedPreferences
    ) {
        val screen = FrameLayout(this)

        val background = android.widget.ImageView(this).apply {
            setImageBitmap(artwork)
            scaleType = android.widget.ImageView.ScaleType.CENTER_CROP
        }
        screen.addView(
            background,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        val overlay = ReferenceOverlayLayout(this)

        fun mappedField(
            hintText: String,
            inputTypeValue: Int,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ): EditText {
            val field = EditText(this).apply {
                hint = hintText
                inputType = inputTypeValue
                AuthUi.styleOverlayField(this@MainActivity, this)
            }
            overlay.addMappedView(field, left, top, right, bottom)
            return field
        }

        val email = mappedField(
            "Email",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            238f, 648f, 664f, 718f
        )
        val password = mappedField(
            "Password",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            238f, 736f, 610f, 805f
        )

        val rememberMe = CheckBox(this).apply {
            text = ""
            isChecked = authPrefs.getBoolean("remember_me", true)
            buttonTintList = ColorStateList.valueOf(AuthUi.violetSoft)
            setPadding(0, 0, 0, 0)
        }
        overlay.addMappedView(rememberMe, 158f, 826f, 194f, 864f)

        val passwordEye = android.view.View(this).apply {
            setOnClickListener {
                val selection = password.selectionStart.coerceAtLeast(0)
                password.transformationMethod =
                    if (password.transformationMethod == null) {
                        android.text.method.PasswordTransformationMethod.getInstance()
                    } else {
                        null
                    }
                password.setSelection(selection.coerceAtMost(password.text.length))
            }
        }
        overlay.addMappedView(passwordEye, 620f, 744f, 690f, 805f)

        val forgotPassword = android.view.View(this).apply {
            setOnClickListener {
                val emailValue = email.text.toString().trim()
                if (!Patterns.EMAIL_ADDRESS.matcher(emailValue).matches()) {
                    email.error = getString(R.string.error_email_for_reset)
                    email.requestFocus()
                    return@setOnClickListener
                }
                auth.sendPasswordResetEmail(emailValue)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.password_reset_sent),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    .addOnFailureListener { error ->
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
        overlay.addMappedView(forgotPassword, 514f, 824f, 705f, 866f)

        val signIn = Button(this).apply {
            text = ""
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setTextColor(android.graphics.Color.TRANSPARENT)
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
                        auth.signInWithEmailAndPassword(emailValue, passwordValue)
                            .addOnSuccessListener { result ->
                                val user = result.user
                                if (user == null) {
                                    isEnabled = true
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
                                        isEnabled = true
                                        openHub()
                                    }
                            }
                            .addOnFailureListener { error ->
                                isEnabled = true
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
        overlay.addMappedView(signIn, 160f, 890f, 707f, 987f)

        val googleSignIn = Button(this).apply {
            text = ""
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setTextColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener { signInWithGoogle(this) }
        }
        overlay.addMappedView(googleSignIn, 165f, 1067f, 699f, 1148f)

        val guest = Button(this).apply {
            text = ""
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setTextColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener { continueAsGuest(this) }
        }
        overlay.addMappedView(guest, 165f, 1157f, 699f, 1238f)

        val createAccount = android.view.View(this).apply {
            setOnClickListener {
                startActivity(Intent(this@MainActivity, CreateAccountActivity::class.java))
            }
        }
        overlay.addMappedView(createAccount, 470f, 1268f, 674f, 1315f)

        screen.addView(
            overlay,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        setContentView(screen)
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
        button.text = "G   " + getString(R.string.sign_in_with_google)
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
