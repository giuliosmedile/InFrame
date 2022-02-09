package com.smeds.inframe

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import com.smeds.inframe.home.FrameHomeActivity
import com.smeds.inframe.home.LeaderHomeActivity
import com.smeds.inframe.setup.LoginActivity
import com.smeds.inframe.setup.OnboarderPresentationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)

//        val intent = Intent(this, DebugActivity::class.java)
//        startActivity(intent)


        // Get the role of the device, if present
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val role : Int = prefs.getInt("role", -1)
        Log.i("Main", "Role: $role")


        // Select the target class: if 0 leader, if 1 frame, else onboarding
        val whichClass = if (role==0) LeaderHomeActivity::class.java else if (role==1) FrameHomeActivity::class.java else OnboarderPresentationActivity::class.java
        val intent = Intent(this, whichClass)
        startActivity(intent)
    }
}