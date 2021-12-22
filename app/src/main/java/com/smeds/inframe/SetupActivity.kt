package com.smeds.inframe

import android.content.DialogInterface
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar


class SetupActivity : AppCompatActivity() {

    private lateinit var phoneView : ImageView
    private lateinit var frameView : ImageView
    private lateinit var subText : TextView
    private var selected : Int = -1                     // Will need this to decide where to go from the setup screen. 0 for leader, 1 for frame
    private lateinit var rootLayout : View
    private lateinit var descriptionTextView : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null) {
            supportActionBar!!.hide()
        }
        setContentView(R.layout.activity_setup)

        // TODO: Login activity prima di setup activity (o fragment?)

        rootLayout = window.decorView.rootView;

        // Find the views
        phoneView = findViewById(R.id.imagePhoneView)
        frameView = findViewById(R.id.imageFrameView)
        subText = findViewById(R.id.textSubtitle)
        descriptionTextView = findViewById(R.id.descriptionTextView)

        // Add event listeners for images
        phoneView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    onTapPhoneView()
                }

            }
            true
        }

        frameView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    onTapFrameView()
                }

            }
            true
        }

        // Set the event listener for fab
        val fab : FloatingActionButton = findViewById(R.id.floatingActionButton)
        fab.setOnClickListener {
            Log.d("INFO", "FAB Setting Clicked")
            fabClicked()
        }

    }

    private fun onTapPhoneView() {
        // First change the opacity of the images
        frameView.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        phoneView.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f) })

        // Then deal with the text
        subText.textSize = 24f
        subText.text = getString(R.string.leader)

        // Set the description text
        descriptionTextView.text = getString(R.string.leaderDescription)

        // And set the choice
        selected = 0

    }

    private fun onTapFrameView() {
        frameView.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(1f) })
        phoneView.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })

        // Then deal with the text
        subText.textSize = 24f
        subText.text = getString(R.string.frame)

        // Set the description text
        descriptionTextView.text = getString(R.string.frameDescription)

        // And set the choice
        selected = 1
    }

    private fun fabClicked() {

        // First check the value of selected
        when (selected) {
            // Something has been selected
            0, 1 -> {
                val result : String = if (selected==0) getString(R.string.leader) else getString(R.string.frame)
                // Use the Builder class for convenient dialog construction
                val builder = AlertDialog.Builder(this)
                builder.setMessage(getString(R.string.confirmationSetup, result))
                    .setPositiveButton(R.string.ok,
                        DialogInterface.OnClickListener { dialog, id ->
                            // Scegli la activity da chiamare, in base alla risposta dell'utente
                            val whichClass = if (selected==0) LeaderHomeActivity::class.java else FrameHomeActivity::class.java
                            val intent : Intent = Intent(this, whichClass)
                            startActivity(intent)
                        })
                    .setNegativeButton(R.string.cancel,
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog so I do nothing lmao
                        })
                // Create the AlertDialog object and return it
                builder.create()
                // Show it!
                builder.show()
            }
            // All other values
            else -> {
                // For safety reason, let's put selected back to -1...
                selected = -1
                // Setup the snackbar
                val snackbar = Snackbar.make(
                    rootLayout,
                    getText(R.string.setupError),
                    Snackbar.LENGTH_LONG
                )
                // Set an action for snack bar
                snackbar.setAction("Hide Me") {
                    // Hide the snack bar
                    snackbar.dismiss()
                }
                snackbar.show()
            }


        }
    }

}