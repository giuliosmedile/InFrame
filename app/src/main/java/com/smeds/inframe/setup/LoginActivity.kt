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
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import android.view.*
import androidx.annotation.RequiresApi
import com.amazonaws.mobileconnectors.cognitoauth.Auth
import com.amplifyframework.auth.options.AuthSignOutOptions
import com.smeds.inframe.model.Device
import com.smeds.inframe.model.User
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import com.google.gson.Gson





class LoginActivity : AppCompatActivity() {

    private val TAG = "Backend"
    lateinit var dialog : AlertDialog
    lateinit var username : String
    lateinit var password : String
    lateinit var prefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Backend.initialize(this)

        // If I'm here, I want to sign out
        if (Amplify.Auth.currentUser != null) {
            val options = AuthSignOutOptions.builder()
                .globalSignOut(true)
                .build()
            Amplify.Auth.signOut(options,
                { Log.i(TAG, "Signed out globally") },
                { Log.e(TAG, "Sign out failed", it) }
            )
        }

        // Start up preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
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
        Log.i(TAG, "inside handlesigninsuccess. Result: ${r.toString()}")
        runOnUiThread {
            dialog.dismiss()
        }

        // Ricevi informazioni dal server
        val username = Amplify.Auth.currentUser.username
        Log.i(TAG, "User: ${username}")

        // Invia JSON del device al server
        val tempUser : User = User(Amplify.Auth.currentUser.username, "")
        val tempDevice : Device = Device(tempUser, windowManager, this)
        val json = tempDevice.toJSONObject()
        json.put("requestID", "login")
        Log.i(TAG, json.toString())
        try {
            val result : String = Backend.sendJson(json)
            Log.i(TAG, "Result JSON: ${result}")
            //val result = "[\"{'screenInches': 5.288655365180974, 'screenWidthPx': 1440, 'screenHeightPx': 2560, 'screenWidthDp': 1440, 'screenHeightDp': 598, 'screenWidthInch': 2.598428964614868, 'density': 554, 'deviceName': 'LGE-LG-H850-ab953d3e91a788da', 'user': 'giulio', 'height': 18.524554703384638, 'width': 6.751833084149496, 'requestID': 'login', 'screenHeightInch': 1.5}\", \"{'screenInches': 6.076911540920186, 'screenWidthPx': 1440, 'screenHeightPx': 3120, 'screenWidthDp': 1440, 'screenHeightDp': 816, 'screenWidthInch': 2.559058427810669, 'density': 562, 'deviceName': 'LGE-LM-G710-a976a6fd3b6bf863', 'user': 'giulio', 'height': 25.492013636176125, 'width': 6.548780036948813, 'requestID': 'login', 'screenHeightInch': 1.5}\", \"{'screenInches': 6.665616156379986, 'screenWidthPx': 1080, 'screenHeightPx': 2340, 'screenWidthDp': 1080, 'screenHeightDp': 802, 'screenWidthInch': 2.7952771186828613, 'density': 386, 'deviceName': 'Xiaomi-Mi 10-61d727e64909aa2e', 'user': 'giulio', 'requestID': 'login', 'screenHeightInch': 1.5}\", \"{'screenInches': 6.376625060356133, 'screenWidthPx': 1080, 'screenHeightPx': 2340, 'screenWidthDp': 1080, 'screenHeightDp': 843, 'screenWidthInch': 2.6771702766418457, 'density': 403, 'deviceName': 'asus-ASUS_I01WD-c85433a8e7d58c40', 'user': 'giulio', 'height': 29.984161498251524, 'width': 7.167240690134577, 'requestID': 'login', 'screenHeightInch': 1.5}\"]"
            // Crea utente (e dispositivi) dalla response del server
            var devicesList = ArrayList<Device>()
            var jsonArray = JSONArray(result)
            for (i in 0 until jsonArray.length()) {
                var j = JSONObject(jsonArray[i].toString())
                var d = Device(j)
                devicesList.add(d)
            }
            val user = User(Amplify.Auth.currentUser.username, "")
            user.devices = devicesList

            val prefsEditor = prefs.edit()
            val gson = Gson()
            val json = gson.toJson(user)

            // Update preferences
            prefsEditor.putString("user", json)
            prefsEditor.putBoolean("isAuthenticated", true)
            prefsEditor.putString("username", user.username)
            prefsEditor.commit()

            // Launch setup activity
            val intent = Intent(this, SetupActivity::class.java)
            startActivity(intent)


        } catch (e : Exception) {
            Log.e(TAG, "Exception occurred: ${e.message}")
        }



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