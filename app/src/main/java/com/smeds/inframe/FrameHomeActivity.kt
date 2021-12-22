package com.smeds.inframe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast

class FrameHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_home)

        // Set tap listener to image
        val imageView = findViewById<ImageView>(R.id.frameHomeImageView)
        imageView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    Toast.makeText(this, "Image tapped", Toast.LENGTH_SHORT).show()
                }

            }
            true
        }
    }
}