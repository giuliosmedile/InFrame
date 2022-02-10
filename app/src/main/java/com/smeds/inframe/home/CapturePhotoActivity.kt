package com.smeds.inframe.home

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ImageSpan
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.smeds.inframe.R
import com.smeds.inframe.setup.Backend
import com.smeds.inframe.setup.DraggableImage
import com.smeds.inframe.setup.LoginActivity
import kotlinx.android.synthetic.main.activity_capture_photo.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class CapturePhotoActivity : AppCompatActivity() {

    private val TAG = "CapturePhoto"

    lateinit var btn: Button
    lateinit var imgView: ImageView
    lateinit var zoomImgView: ImageView
    lateinit var zoomClass: DraggableImage
    lateinit var pictureImagePath : String
    lateinit var dialog : AlertDialog
    val SELECT_PICTURE: Int = 200
    val SELECT_BACKGROUND_PICTURE : Int = 300

    var foregroundSelected = false
    var backgroundSelected = true

    lateinit var fileBackground : File
    lateinit var fileForeground : File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_capture_photo)

        // Init backend
        Backend.initialize(this)

        //btn = findViewById(R.id.foregroundButton)
        imgView = findViewById(R.id.imageView)
        zoomImgView = findViewById(R.id.zoomImageView)
        zoomClass = findViewById(R.id.largeImage)

        // Check for permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100);

        // Set the hint text asking for the photo
        setHintText(getString(R.string.hint_camera),
        getString(R.string.del_camera),
        R.drawable.ic_menu_camera)
    }


    fun chooseImage(view: View) {
        // Create an instance of the intent of the type image
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    fun chooseImageBackground(view : View) {
        // Function to select the background image

        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "$timeStamp.jpg"
        val storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES
        )
        pictureImagePath = storageDir.absolutePath + "/" + imageFileName
        val file = File(pictureImagePath)
        val outputFileUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", file)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        cameraIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(cameraIntent, SELECT_BACKGROUND_PICTURE)
    }

    /**
     * Function to send data to server when calculation is over.
     * Data is:
     *  - a JSON containing the translation and scale factors of the superimposed image over the original one, wrt the original
     *  - the two images at full scale
     */
    @RequiresApi(Build.VERSION_CODES.N)

    fun confirm(view : View) {

        // Dump the matrices into the arrays
        val aMatrix = FloatArray(9)
        val aZoomM = FloatArray(9); val aImageM = FloatArray(9)
        zoomClass.mMatrix?.getValues(aZoomM)
        imgView.imageMatrix.getValues(aImageM)

        // Calculate scaling factor: I want to have coordinates and scaling wrt the original image size
        val scalingFactor : Float = imgView.drawable.intrinsicWidth.toFloat() / imgView.width
        Log.i("TEST", "scalingfactor: $scalingFactor, intw: ${imgView.drawable.intrinsicWidth}, imgvw: ${imgView.width}")
        for (i in 0..8) {
            aMatrix[i] = (aZoomM[i] - aImageM[i]) * scalingFactor
        }
        // Get the bounds of the scaled image, so I know how big it actually is
        val bounds = RectF()
        val drawable: Drawable = zoomClass.drawable
        zoomClass.imageMatrix.mapRect(bounds, RectF(drawable.bounds))

        // Set the scaling values, so that the size is defined as a proportion of the source
        aMatrix[0] = bounds.width() / imgView.width
        aMatrix[4] = aMatrix[0]

        // Now create the json containing the matrix data
        val json = JSONObject()
            .put("requestID", "photo")
            .put("scaleX", aMatrix[0])
            .put("scaleY", aMatrix[4])
            .put("translateX", aMatrix[2])
            .put("translateY", aMatrix[5])
            .put("fileForeground", fileForeground.name)
            .put("fileBackground", fileBackground.name)
            .put("fileBackgroundWidth", imgView.drawable.intrinsicWidth)
            .put("fileBackgroundHeight", imgView.drawable.intrinsicHeight)
            .put("fileForegroundWidth", zoomClass.drawable.intrinsicWidth)
            .put("fileForegroundHeight", zoomClass.drawable.intrinsicHeight)
            .put("user", PreferenceManager.getDefaultSharedPreferences(this).getString("username", ""))

        //Log.i("TEST", "JSON: ${json.toString(1)}")


        GlobalScope.launch {
            val tasks = listOf(
                // Invia le due immagini al server
                launch { Backend.uploadFile(fileForeground, fileForeground.name) },
                launch { Backend.uploadFile(fileBackground, fileBackground.name) }
            )
            tasks.joinAll()
        }


        // Invia json al server
        Backend.sendJson(json)
        Log.i(TAG, "Dopo aver inviato al server: ${json.toString(1)}")
        hintsTextView.text = "Sent data to server succesfully"
    }

    fun clear(view : View) {

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.clearPhoto))
            .setMessage(getString(R.string.clearPhotoMsg)) // Specifying a listener allows you to take an action before dismissing the dialog.
            // The dialog is automatically dismissed when a dialog button is clicked.
            .setPositiveButton(android.R.string.yes,
                DialogInterface.OnClickListener { _, which ->
                    // Discard the photos
                    runOnUiThread {
                        zoomClass.setImageResource(android.R.color.transparent)
                        imgView.setImageResource(android.R.color.transparent)
                        zoomImgView.setImageResource(android.R.color.transparent)

                        // Hide the FABs
                        clearFab.visibility = View.INVISIBLE
                        nextFab.visibility = View.INVISIBLE

                        // Set the hint text asking for the photo
                        setHintText(
                            getString(R.string.hint_camera),
                            getString(R.string.del_camera),
                            R.drawable.ic_menu_camera)
                    }
                    // Reset the bools
                    backgroundSelected = false
                    foregroundSelected = false
                }) // A null listener allows the button to dismiss the dialog and take no further action.
            .setNegativeButton(android.R.string.no, null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }


    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // This function is triggered when user selects the image from the imageChooser or the camera
        super.onActivityResult(requestCode, resultCode, data)


        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {

                val selectedImageUri = data!!.data
                try {
                    zoomClass.setImageURI(selectedImageUri)
                    zoomClass.imageAlpha = 99
                    foregroundSelected = true

                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
                    fileForeground = File.createTempFile("temp", ".png")
                    val fos = FileOutputStream(fileForeground)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                    fos.close()



                } catch (e : Exception) {
                   Log.e(TAG, "Caught exception: ${e.message}")
                }
            } else if (requestCode == SELECT_BACKGROUND_PICTURE) {

                val imgFile = File(pictureImagePath);
                if(imgFile.exists()){
                    val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)

                    // Rotate the image, if needed
                    val finalBitmap = rotateImageIfRequired(this, myBitmap, Uri.fromFile(imgFile))
                    imgView.setImageBitmap(finalBitmap)
                    backgroundSelected = true
                }
                photoFromGalleryFab.visibility = View.VISIBLE
                fileBackground = imgFile
            }
            // Update the hint text
            if (!foregroundSelected) {
                setHintText(
                    getString(R.string.hint_gallery),
                    getString(R.string.del_gallery),
                    R.drawable.ic_menu_gallery
                )
            } else if (foregroundSelected && backgroundSelected){
                nextFab.visibility = View.VISIBLE
                clearFab.visibility = View.VISIBLE

                setHintText(
                    getString(R.string.hint_next),
                    getString(R.string.del_next),
                    R.drawable.ic_baseline_navigate_next_24
                )
            }
        }
    }

    /**
     * Rotate an image if required. This is needed because sometimes the Camera API will randomically rotate the photo without user consent
     *
     * @param img           The image bitmap
     * @param selectedImage Image URI
     * @return The resulted Bitmap after manipulation
     */
    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap? {
        val input: InputStream? = context.contentResolver.openInputStream(selectedImage)
        val ei: ExifInterface
        if (Build.VERSION.SDK_INT > 23) ei = ExifInterface(input!!) else ei =
            ExifInterface(selectedImage.path!!)
        return when (ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270)
            else -> img
        }
    }

    /**
     * Function to rotate an image
     */
    private fun rotateImage(img: Bitmap, degree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    private fun setHintText(mText : String, mDelimiter : String, mDrawable : Int) {
        val drawable = ContextCompat.getDrawable(this, mDrawable)
        drawable!!.setBounds(0, 0, 100, 100)

        var text = mText
        val delimiter = mDelimiter

        val icon_index = text.indexOf(mDelimiter)
        text = text.replace(delimiter, " ")

        val span: Spannable = SpannableString(text)
        val image = ImageSpan(drawable!!, ImageSpan.ALIGN_BASELINE)
        span.setSpan(image, icon_index, icon_index + 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        hintsTextView.text = span
    }

    /* Function to correctly set a Process Dialog, while the app is communicating with the server
       It just creates and displays the dialog, nothing more, nothing less
       Stolen from StackOverflow
     */
    private fun setProgressDialog() {
        val llPadding = 30
        val ll = LinearLayout(this)
        ll.orientation = LinearLayout.HORIZONTAL
        ll.setPadding(llPadding, llPadding, llPadding, llPadding)
        ll.gravity = Gravity.CENTER
        var llParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        ll.layoutParams = llParam
        val progressBar = ProgressBar(this)
        progressBar.isIndeterminate = true
        progressBar.setPadding(0, 0, llPadding, 0)
        progressBar.layoutParams = llParam
        llParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        llParam.gravity = Gravity.CENTER
        val tvText = TextView(this)
        tvText.text = getString(R.string.loading)
        tvText.setTextColor(Color.parseColor("#000000"))
        tvText.textSize = 20f
        tvText.layoutParams = llParam
        ll.addView(progressBar)
        ll.addView(tvText)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setView(ll)
        dialog = builder.create()
        dialog.show()
        val window: Window? = dialog?.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog.window?.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            dialog.window?.attributes = layoutParams
        }
    }

}