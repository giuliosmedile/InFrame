package com.smeds.inframe.setup

import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.smeds.inframe.R

class QRDisplayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        // This will be the name of the device, will be the same in the DB eventually
        // TODO sostituire smeds con il nome dell'utente da login
        // TODO parametrizzare il tutto in una struttura di setting
        var deviceName : String = Build.MANUFACTURER + "-" + Build.MODEL + "-" + "smeds"

        val imageView = findViewById<ImageView>(R.id.qrImageView)
        val bitmap = generateQR(deviceName, 256)

        imageView.setImageBitmap(bitmap)
    }


    /**
     * Function to generate a QR
     * @param content The string to encode
     * @param size The maximum size of the QR
      */
    fun generateQR(content: String?, size: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val barcodeEncoder = BarcodeEncoder()
            bitmap = barcodeEncoder.encodeBitmap(
                content,
                BarcodeFormat.QR_CODE, size, size
            )
        } catch (e: WriterException) {
            e.message?.let { Log.e("generateQR()", it) }
        }
        return bitmap
    }


}

