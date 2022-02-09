package com.smeds.inframe.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.smeds.inframe.R
import com.smeds.inframe.databinding.ActivityLeaderHomeBinding
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.smeds.inframe.MainActivity
import kotlin.system.exitProcess


class LeaderHomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityLeaderHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLeaderHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarLeaderHome.toolbar)

        binding.appBarLeaderHome.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_leader_home)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.leader_home, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_leader_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.smeds.inframe.R.id.action_settings -> {
                clickOnSettings()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun clickOnSettings() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Reset all settings?")
            .setPositiveButton(
                com.smeds.inframe.R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    // Reset sharedsettings
                    var prefs = PreferenceManager.getDefaultSharedPreferences(this)
                    var editor = prefs.edit()
                    editor.clear()
                    editor.commit()

                    // Restart app
                    restartApp()
                })
            .setNegativeButton(
                com.smeds.inframe.R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog so I do nothing lmao
                })
        // Create the AlertDialog object and return it
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        builder.create()
        // Show it!
        builder.show()
    }

    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        val mPendingIntentId: Int = 1
        val mPendingIntent = PendingIntent.getActivity(
            this,
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
        exitProcess(0)
    }
}