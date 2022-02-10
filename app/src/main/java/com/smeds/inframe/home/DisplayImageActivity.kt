package com.smeds.inframe.home

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.withMatrix
import com.smeds.inframe.R
import com.smeds.inframe.data.DeviceInfo
import com.smeds.inframe.data.MatrixTransform
import com.smeds.inframe.setup.Backend
import kotlinx.android.synthetic.main.activity_display_image.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class DisplayImageActivity : AppCompatActivity() {

    private val TAG = "DisplayImage"
    lateinit var resultString: String
    lateinit var prefs : SharedPreferences
    lateinit var imageView: ImageView
    lateinit var imageFile : File
    var sizeOfImageInInchesX : Float = 0f
    var sizeOfImageInInchesY : Float = 0f
    var translateXInch : Float = 0f
    var translateYInch : Float = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_image)
        Log.i(Backend.TAG, "sono in displayImage")

        val extras = intent.extras
        if (extras?.getString("result") == null) {
            Log.e(TAG, "extras are null, exiting")
            finish()
        } else {
            resultString = extras.getString("result")!!
        }

        imageView = findViewById<ImageView>(R.id.displayImage)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        val json = decodeJSON()
        if (json == null) {
            Log.e(TAG, "Error in reading from json")
            finish()
        }

        displayImage(json)
    }

    private fun decodeJSON() : JSONObject? {

        // Decode result string
        val jsonArray = JSONArray(resultString)
        val deviceName = prefs.getString("deviceName", "")
        for (i in 0 until jsonArray.length()) {
            // If this phone is present in the result, return it
            val item = jsonArray.getJSONObject(i)
            if (item.getString("deviceName") == deviceName) return item
        }
        return null
    }

    private fun displayImage(json: JSONObject?) {

        // Get image from server
        val filename = json?.getString("foregroundImage")?.dropLast(4)
        imageFile = Backend.downloadFile(filename!!)
        val bitmap = BitmapFactory.decodeFile(imageFile.path)

        // Get information from json
        sizeOfImageInInchesX = json?.getDouble("sizeOfImageInInchesX")!!.toFloat()
        sizeOfImageInInchesY = json.getDouble("sizeOfImageInInchesY").toFloat()
        translateXInch = json.getDouble("translateXInch").toFloat()
        translateXInch = json.getDouble("translateYInch").toFloat()

        val d = DeviceInfo(windowManager)
        val matrix = MatrixTransform.calcMatrixForImage(d, bitmap, translateXInch, translateYInch, sizeOfImageInInchesX, sizeOfImageInInchesY)

        val blackBitmap = Bitmap.createBitmap(d.screenWidthDp, d.screenHeightDp, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(blackBitmap)
        canvas.drawARGB(255, 0, 0, 0)
        imageView.setImageBitmap(bitmap)
        canvas.withMatrix(matrix) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

    }
}