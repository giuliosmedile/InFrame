package com.smeds.inframe.setup

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.transition.Visibility
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withMatrix
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.result.StorageDownloadFileResult
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.smeds.inframe.R
import com.smeds.inframe.data.DeviceInfo
import com.smeds.inframe.data.MatrixTransform
import kotlinx.android.synthetic.main.activity_qr.*
import com.google.zxing.EncodeHintType
import com.smeds.inframe.home.DisplayImageActivity
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.Exception
import java.util.*


class QRDisplayerActivity : AppCompatActivity() {

    lateinit var deviceName : String
    lateinit var prefs : SharedPreferences
    var job : Job? = null
    lateinit var jsonString : String
    lateinit var imageFile : File
    var sizeOfImageInInchesX : Float = 0f
    var sizeOfImageInInchesY : Float = 0f
    var translateXInch : Float = 0f
    var translateYInch : Float = 0f
    lateinit var imageView : ImageView
    lateinit var json : JSONObject

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        imageView = findViewById(R.id.qrImageView)
        setQR()
        var result : String = ""
        job = GlobalScope.launch (IO) {
            result = makeCall()
//            Toast.makeText(applicationContext, "received correct json", Toast.LENGTH_SHORT).show()
            Log.e(Backend.TAG, "RECEIVED JSON OMG $result")

            withContext(Main) {
                Backend.initialize(applicationContext)
                jsonString = result
                setFinalImage()
            }
        }

    }

    private fun setQR() {
        // This will be the name of the device, will be the same in the DB eventually
        // TODO sostituire smeds con il nome dell'utente da login
        // TODO parametrizzare il tutto in una struttura di setting
        val android_id = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        deviceName = Build.MANUFACTURER + "-" + Build.MODEL + "-" + android_id

        val imageView = findViewById<ImageView>(R.id.qrImageView)
        val d = DeviceInfo(windowManager)
        val image = generateQR(deviceName, 512)
        val bitmap = Bitmap.createBitmap(d.screenWidthDp, d.screenHeightDp, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        imageView.setImageBitmap(bitmap)

        val matrix = MatrixTransform.calcMatrixForImage(
            d,
            image!!,
            0f,
            0f,
            0.787402f,
            0.787402f
        )

        // Questo blocco serve perchè altrimenti l'immagine non è perfettamente quadrata...
        // Anche se la funzione calcMatrixForImage funziona alla perfezione ^_^
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)
        matrixValues[4] = matrixValues[0]
        matrix.setValues(matrixValues)
        // E questo serve per centrare il QR
        matrix.postTranslate((canvas.width) / 2f, (canvas.height) / 2f)
        matrix.postTranslate(-image.width / 2f * matrixValues[0], -image.height / 2f * matrixValues[0])

        Log.i("MATRIX", "$matrix")
        Log.i("MATRIX", "${d.toString()}")

        // Tutto lo schermo bianco, poi l'immagine del QR
        canvas.drawARGB(255, 255, 255, 255)
        canvas.withMatrix(matrix) {
            canvas.drawBitmap(image, 0f, 0f, null)
        }

        Log.i("MATRIX", "${canvas.width} - ${canvas.height}")
    }

    /**
     * Function to generate a QR
     * @param content The string to encode
     * @param size The maximum size of the QR
      */
    fun generateQR(content: String?, size: Int): Bitmap? {
        var bitmap: Bitmap? = null
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        //hints.put(EncodeHintType.CHARACTER_SET, encoding);
        //hints.put(EncodeHintType.CHARACTER_SET, encoding);
        hints[EncodeHintType.MARGIN] = 0 /* default = 4 */
        try {
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.encodeBitmap(
                content,
                BarcodeFormat.QR_CODE, size, size, hints
            )
        } catch (e: WriterException) {
            e.message?.let { Log.e("generateQR()", it) }
        }

        return bitmap
    }


    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun makeCall() : String {

        val jsonRequest = JSONObject()
            .put("user", prefs.getString("username", ""))
            .put("deviceName", deviceName)
            .put("requestID", "output")

        while (true) {
            delay(3000L)
            val response = Backend.sendJson(jsonRequest)
            Log.i("TEST", response)
            if (response == "none" || response.isEmpty()) continue
            else return response
        }
    }

    override fun onPause() {
        job?.cancel()
        job = null
        super.onPause()
    }

    private fun setFinalImage() {
        try {
            Log.i(Backend.TAG, "Dentro finalimage")
            qrImageView.visibility = View.INVISIBLE

            // Chiama decode
            json = decodeJSON(jsonString)!!
            displayImage(json)
        } catch (e : Exception) {
            Log.e(Backend.TAG, "dentro finalimage: $e")
        }
    }

    private fun decodeJSON(resultString : String) : JSONObject? {

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
        val filename = json?.getString("foregroundImage")

        lifecycleScope.launch {
            var file = File.createTempFile(filename, Backend.getFileExtension(filename!!))

            Amplify.Storage.downloadFile(filename, file,
                {res : StorageDownloadFileResult -> Log.i(Backend.TAG, "Succesfully downloaded $filename, $res"); handleResultImage(file)},
                {Log.e(Backend.TAG, "Download failed $filename")})
        }



    }

    private fun handleResultImage(file : File) {
        val bitmap = BitmapFactory.decodeFile(file.path)

        // Get information from json
        sizeOfImageInInchesX = json?.getDouble("sizeOfImageInInchesX")!!.toFloat()///5*2
        sizeOfImageInInchesY = json.getDouble("sizeOfImageInInchesY").toFloat()///5*2
        translateXInch = json.getDouble("translateXInch").toFloat()//* 100
        translateYInch = json.getDouble("translateYInch").toFloat()//* 100

        Log.i(Backend.TAG,"sx: $sizeOfImageInInchesX, sy: $sizeOfImageInInchesY, tx: $translateXInch, ty: $translateYInch")

        val d = DeviceInfo(windowManager)
        var matrix = MatrixTransform.calcMatrixForImage(d, bitmap, translateXInch, translateYInch, sizeOfImageInInchesX, sizeOfImageInInchesY)

        val bm = Bitmap.createBitmap(d.screenWidthDp, d.screenHeightDp, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bm)
        imageView.setImageBitmap(bm)

        matrix = Matrix()
        // Rendi l'immagine tanto larga quanto il telefono
        val originalWidth: Float = bitmap.getWidth().toFloat()
        val originalHeight: Float = bitmap.getHeight().toFloat()
        val phoneScaleX: Float = d.screenWidthDp / originalWidth
        val phoneScaleY = d.screenHeightDp / originalHeight

        matrix.postScale(phoneScaleX, phoneScaleY)
        // Scala l'immagine per arrivare alle dimensioni fisiche desiderate
        val finalScaleX = (sizeOfImageInInchesX / d.screenWidthInch).toFloat()
        val finalScaleY = (sizeOfImageInInchesY / d.screenHeightInch).toFloat()
        matrix.postScale(finalScaleX, finalScaleY)
        val finalTranslateX = (originalWidth / (sizeOfImageInInchesX)) * translateXInch
        val finalTranslateY = (originalHeight / (sizeOfImageInInchesY)) * translateYInch
        matrix.preTranslate(finalTranslateX, finalTranslateY)

        canvas.withMatrix(matrix) {
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
        imageView.visibility = View.VISIBLE



        Log.i(Backend.TAG, "matrix: ${matrix.toString()}, bounds: ${imageView.clipBounds}")
    }

}

