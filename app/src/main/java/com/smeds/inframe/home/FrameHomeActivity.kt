package com.smeds.inframe.home

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import com.smeds.inframe.R
import com.smeds.inframe.setup.OnboarderPresentationActivity
import com.smeds.inframe.setup.QRDisplayerActivity

class FrameHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_home)

        // Set tap listener to image
        val imageView = findViewById<ImageView>(R.id.frameHomeImageView)
        imageView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    val intent = Intent(this, QRDisplayerActivity::class.java)
                    startActivity(intent)
                }

            }
            true
        }
    }
}