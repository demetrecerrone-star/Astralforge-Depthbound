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

class CreateAccountActivity : Activity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exactArtwork = android.graphics.BitmapFactory.decodeResource(
            resources,
            R.drawable.create_exact
        )
        showExactCreateAccount(exactArtwork)
    }

    private fun showExactCreateAccount(artwork: android.graphics.Bitmap) {
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
                AuthUi.styleOverlayField(this@CreateAccountActivity, this)
            }
            overlay.addMappedView(field, left, top, right, bottom)
            return field
        }

        val displayName = mappedField(
            "Username",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
            238f, 622f, 680f, 699f
        )
        val email = mappedField(
            "Email",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
            238f, 716f, 680f, 794f
        )
        val password = mappedField(
            "Password",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            238f, 813f, 610f, 891f
        )
        val confirmPassword = mappedField(
            "Confirm Password",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
            238f, 910f, 610f, 989f
        )

        fun addPasswordEye(
            field: EditText,
            left: Float,
            top: Float,
            right: Float,
            bottom: Float
        ) {
            val eye = View(this).apply {
                setOnClickListener {
                    val selection = field.selectionStart.coerceAtLeast(0)
                    field.transformationMethod =
                        if (field.transformationMethod == null) {
                            android.text.method.PasswordTransformationMethod.getInstance()
                        } else {
                            null
                        }
                    field.setSelection(selection.coerceAtMost(field.text.length))
                }
            }
            overlay.addMappedView(eye, left, top, right, bottom)
        }

        addPasswordEye(password, 620f, 824f, 690f, 888f)
        addPasswordEye(confirmPassword, 620f, 922f, 690f, 985f)

        val createButton = Button(this).apply {
            text = ""
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
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
                    else -> createAccount(
                        nameValue = nameValue,
                        emailValue = emailValue,
                        passwordValue = passwordValue,
                        receiveUpdates = false,
                        createButton = this
                    )
                }
            }
        }
        overlay.addMappedView(createButton, 160f, 1018f, 705f, 1117f)

        val googleButton = Button(this).apply {
            text = ""
            setBackgroundColor(Color.TRANSPARENT)
            setTextColor(Color.TRANSPARENT)
            setOnClickListener { signInWithGoogle(this) }
        }
        overlay.addMappedView(googleButton, 160f, 1206f, 705f, 1287f)

        val signIn = View(this).apply {
            setOnClickListener { finish() }
        }
        overlay.addMappedView(signIn, 535f, 1326f, 654f, 1383f)

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
                            val googleCredential =
                                GoogleIdTokenCredential.createFrom(credential.data)
                            firebaseAuthWithGoogle(googleCredential.idToken, button)
                        } catch (error: Exception) {
                            button.isEnabled = true
                            Toast.makeText(
                                this@CreateAccountActivity,
                                getString(
                                    R.string.google_sign_in_failed_with_reason,
                                    error.localizedMessage ?: getString(R.string.unknown_error)
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        button.isEnabled = true
                        Toast.makeText(
                            this@CreateAccountActivity,
                            getString(R.string.google_sign_in_failed),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(error: GetCredentialException) {
                    button.isEnabled = true
                    Toast.makeText(
                        this@CreateAccountActivity,
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
        auth.signInWithCredential(firebaseCredential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    button.isEnabled = true
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
                    "lastLoginAt" to FieldValue.serverTimestamp()
                )

                if (result.additionalUserInfo?.isNewUser == true) {
                    profile["createdAt"] = FieldValue.serverTimestamp()
                }

                firestore.collection("players")
                    .document(user.uid)
                    .set(profile, SetOptions.merge())
                    .addOnCompleteListener {
                        button.isEnabled = true
                        getSharedPreferences("auth_prefs", MODE_PRIVATE)
                            .edit()
                            .putBoolean("remember_me", true)
                            .apply()
                        openHub()
                    }
            }
            .addOnFailureListener { error ->
                button.isEnabled = true
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
