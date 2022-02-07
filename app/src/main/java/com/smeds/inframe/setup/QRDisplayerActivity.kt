package com.smeds.inframe.setup

import android.content.res.Configuration
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.withMatrix
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.smeds.inframe.R
import com.smeds.inframe.data.DeviceInfo
import com.smeds.inframe.data.MatrixTransform
import kotlinx.android.synthetic.main.activity_qr.*
import com.google.zxing.EncodeHintType
import java.util.*


class QRDisplayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr)

        // This will be the name of the device, will be the same in the DB eventually
        // TODO sostituire smeds con il nome dell'utente da login
        // TODO parametrizzare il tutto in una struttura di setting
        val android_id = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ANDROID_ID
        )
        var deviceName : String = Build.MANUFACTURER + "-" + Build.MODEL + "-" + android_id

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
            2f,
            2f
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




}

