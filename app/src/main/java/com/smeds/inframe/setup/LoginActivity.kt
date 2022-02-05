package com.smeds.inframe.setup

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.core.Amplify
import com.smeds.inframe.R
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.launch
import android.widget.LinearLayout

import android.widget.ProgressBar
import android.app.AlertDialog
import android.os.Build
import android.view.*
import androidx.annotation.RequiresApi
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.smeds.inframe.model.Device
import com.smeds.inframe.model.User
import java.lang.Exception


class LoginActivity : AppCompatActivity() {

    private val TAG = "Backend"
    lateinit var dialog : AlertDialog
    lateinit var username : String
    lateinit var password : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Backend.initialize(this)

        // If I'm here, I want to sign out
        val options = AuthSignOutOptions.builder()
            .globalSignOut(true)
            .build()
        Amplify.Auth.signOut(options,
            { Log.i(TAG, "Signed out globally") },
            { Log.e(TAG, "Sign out failed", it) }
        )

    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun signIn(view: View) {
        // Handle the signIn procedure (username/password)

        username = findViewById<EditText>(R.id.email_edTxt).text.toString()
        password = findViewById<EditText>(R.id.passwd_edTxt).text.toString()

        Log.i(TAG, "$username $password ${Amplify.Auth.currentUser.toString()}")
        // Reset the red outline of the textboxes, if present
        filledTextFieldEmail.error = null
        filledTextFieldPassword.error = null

        // Validate input
        if (!isInputChecked()) return

        setProgressDialog()

        lifecycleScope.launch {
            Amplify.Auth.signIn(username, password,
                { result: AuthSignInResult -> Log.i(TAG, result.toString()); handleSigninSuccess(result, errorTextView)},
                { error: AuthException -> Log.e(TAG, error.toString()); handleSignupException(error, errorTextView)})
        }
    }

    fun signUp(view: View) {
        // Handle the signUp procedure (username/password)

        username = findViewById<EditText>(R.id.email_edTxt).text.toString()
        password = findViewById<EditText>(R.id.passwd_edTxt).text.toString()

        // Reset the red outline of the textboxes, if present
        filledTextFieldEmail.error = null
        filledTextFieldPassword.error = null

        // Validate input
        if (!isInputChecked()) return

        setProgressDialog()
        lifecycleScope.launch {
            val attributes: AuthSignUpOptions = AuthSignUpOptions.builder()
                .userAttribute(AuthUserAttributeKey.email(), "valerio.marocca@gmail.com")
                .build()

            try {
                Amplify.Auth.signUp(username, password, attributes,
                    { result : AuthSignUpResult -> Log.i(TAG, "yay"); handleSignupSuccess(result, errorTextView)},
                    { error : AuthException -> Log.i(TAG, "nay"); handleSignupException(error, errorTextView) })
                Log.i(TAG, "Result")
            } catch (error: AuthException) {
                Log.e(TAG, "Sign up failed", error)
            }
        }
    }


    /* ------------------------------------------------------------

                        LOGIN/SIGNUP HANDLERS

    ------------------------------------------------------------ */

    private fun handleSignupException(e : AuthException, errorTextView: TextView) {
        Log.i(TAG, "inside handlesignupexception")
        Log.i(TAG, "Exception: ${e.toString()}")
        runOnUiThread {
            if (e is AuthException.UsernameExistsException) {
//            Toast.makeText(callingActivity, "Username already exists", Toast.LENGTH_LONG).show()
                Log.i(TAG, "username")
                errorTextView.text = e.message
            } else {
//            Toast.makeText(callingActivity, "Generic error. Try signing up again.", Toast.LENGTH_LONG).show()
                Log.i(TAG, "not username")
                errorTextView.text = e.message
            }
            dialog.dismiss()
        }

    }

    private fun handleSignupSuccess(r : AuthSignUpResult, errorTextView: TextView) {
        Log.i(TAG, "inside handlesignupsuccess")
        runOnUiThread {
            errorTextView.text = r.toString()
            dialog.dismiss()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun handleSigninSuccess(r : AuthSignInResult, errorTextView: TextView) {
        Log.i(TAG, "inside handlesigninsuccess")
        runOnUiThread {
            errorTextView.text = r.toString()
            dialog.dismiss()
        }

        // Ricevi informazioni dal server
        val username = Amplify.Auth.currentUser.username
        Log.e(TAG, "User: ${username}")

        // Invia JSON del device al server
        val user : User = User(Amplify.Auth.currentUser.username, "")
        val device : Device = Device(user, windowManager)
        val json = device.toJSONObject()
        Log.i(TAG, json.toString())
        try {
            Backend.sendJson(json)
        } catch (e : Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}")
        }

        // TODO: Ricevi JSON dal server per creare User


    }


    /* ------------------------------------------------------------

                            UI MISCELLANEA

    ------------------------------------------------------------ */

    /*
    Function to check if the text fields are correctly filled in
    Mainly for UI purposes
     */
    private fun isInputChecked() : Boolean {
        var result = true
        if (TextUtils.isEmpty(username)) {
            Log.i(TAG, "Empty username or password fields.")
            filledTextFieldEmail.error = getString(R.string.usernameError)
            result = false
        }
        if (TextUtils.isEmpty(password)) {
            Log.i(TAG, "Empty username or password fields.")
            filledTextFieldPassword.error = getString(R.string.passwordError)
            result = false
        }
        return result
    }

    /* Function to correctly set a Process Dialog, while the app is communicating with the server
       It just creates and displays the dialog, nothing more, nothing less
       Stolen from StackOverflow
     */
    private fun setProgressDialog() {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = getString(R.string.loading)
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setView(ll)
        dialog = builder.create()
        dialog?.show()
        val window: Window? = dialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
    }
}