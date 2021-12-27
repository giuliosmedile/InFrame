package com.smeds.inframe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.smeds.inframe.setup.LoginActivity
import com.smeds.inframe.setup.OnboarderPresentationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Una volta fatto login, bisogna reindirizzare l'utente all'activity giusta
        val intent = Intent(this, DebugActivity::class.java)
        startActivity(intent)
    }
}