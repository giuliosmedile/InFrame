package com.smeds.inframe.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.smeds.inframe.R
import com.smeds.inframe.databinding.FragmentHomeBinding
import com.smeds.inframe.home.CapturePhotoActivity
import com.smeds.inframe.setup.QRDisplayerActivity
import android.app.AlarmManager

import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.smeds.inframe.MainActivity
import com.smeds.inframe.home.FrameHomeActivity
import com.smeds.inframe.home.LeaderHomeActivity
import kotlin.system.exitProcess


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val imageView = binding.frameHomeImageView2

        imageView.setOnTouchListener {  _, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    val intent = Intent(activity?.applicationContext, CapturePhotoActivity()::class.java)
                    startActivity(intent)
                }

            }
            true }

        return root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun clickOnSettings(view : View) {
        val builder = AlertDialog.Builder(context!!)
        builder.setMessage("Reset all settings?")
            .setPositiveButton(
                R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    restartApp()
                })
            .setNegativeButton(
                R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog so I do nothing lmao
                })
        // Create the AlertDialog object and return it
        builder.create()
        // Show it!
        builder.show()
    }

    private fun restartApp() {
        val intent = Intent(context, MainActivity::class.java)
        val mPendingIntentId: Int = 1
        val mPendingIntent = PendingIntent.getActivity(
            context,
            mPendingIntentId,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val mgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mgr[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
        exitProcess(0)
    }
}