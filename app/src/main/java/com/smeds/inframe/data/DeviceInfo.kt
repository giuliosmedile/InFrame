package com.smeds.inframe.data

import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.Display
import android.view.WindowManager
import org.json.JSONObject
import kotlin.math.sqrt


class DeviceInfo (private val windowManager: WindowManager) {

    var screenInches : Double = 0.0             // Dimension of screen in inches
    var screenWidthPx : Int = 0                 // Dimension of screen width in pixels
    var screenHeightPx : Int = 0                // Dimension of screen height in pixels
    var screenWidthDp : Int = 0                 // Screen width in dp
    var screenHeightDp : Int = 0                // Screen height in dp
    var screenWidthInch : Double = 0.0
    var screenHeightInch : Double = 0.0
    var density : Int = 0

    init {
        val display = windowManager.defaultDisplay
        val displayMetrics = DisplayMetrics()
        display.getMetrics(displayMetrics)


        // since SDK_INT = 1;


        // since SDK_INT = 1;
        var mWidthPixels = displayMetrics.widthPixels
        var mHeightPixels = displayMetrics.heightPixels

        // includes window decorations (statusbar bar/menu bar)

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17) {
            try {
                mWidthPixels = Display::class.java.getMethod("getRawWidth").invoke(display) as Int
                mHeightPixels = Display::class.java.getMethod("getRawHeight").invoke(display) as Int
            } catch (ignored: Exception) {
            }
        }

        // includes window decorations (statusbar bar/menu bar)

        // includes window decorations (statusbar bar/menu bar)
        if (Build.VERSION.SDK_INT >= 17) {
            try {
                val realSize = Point()
                Display::class.java.getMethod("getRealSize", Point::class.java)
                    .invoke(display, realSize)
                mWidthPixels = realSize.x
                mHeightPixels = realSize.y
            } catch (ignored: Exception) {
            }
        }

        val dm = DisplayMetrics()
        windowManager.getDefaultDisplay().getMetrics(dm)
        val x = Math.pow((mWidthPixels / dm.xdpi).toDouble(), 2.0)
        val y = Math.pow((mHeightPixels / dm.ydpi).toDouble(), 2.0)

        screenWidthPx = mWidthPixels
        screenHeightPx = mHeightPixels
        screenInches = Math.sqrt(x + y)

        var density = dm.density
        screenHeightDp = (dm.heightPixels / density).toInt()
        screenWidthDp = (dm.widthPixels / density).toInt()

        screenWidthInch = sqrt(x)
        screenHeightInch = sqrt(y)

        this.density =
            (dm.xdpi).toInt()

        Log.v(
            "INFO",
            "Width: ${screenWidthPx}px, Height: ${screenHeightPx}px, Diagonal: ${screenInches}\""
        )
        Log.v("INFO", "Width: ${screenWidthDp}dp, Height: ${screenHeightDp}dp")
    }

    override fun toString(): String {
        return "[Inches] w: ${"%.2f".format(screenWidthInch)}, h: ${"%.2f".format(screenHeightInch)}, d: ${"%.2f".format(screenInches)} \n" +
                "[Pixels] w: ${screenWidthPx}, h: ${screenHeightPx}\n" +
                "[Dp] w: ${screenWidthDp}, h: ${screenHeightDp}, density: $density"
    }

    fun toJsonObject() : JSONObject {
        return JSONObject()
            .put("screenInches", screenInches)
            .put("screenWidthPx", screenWidthPx)
            .put("screenHeightPx", screenHeightPx)
            .put("screenWidthDp", screenWidthPx)
            .put("screenHeightDp", screenHeightDp)
            .put("screenWidthInch", screenWidthInch)
            .put("density", density)
    }



}