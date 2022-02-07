package com.smeds.inframe.setup

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.s3.AWSS3StoragePlugin
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.Socket
import java.net.URL
import java.util.stream.Collectors


object Backend {

    private const val TAG = "Backend"

    fun initialize(applicationContext: Context) : Backend {
        // Initializes all the needed plugins
        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSS3StoragePlugin())
            Amplify.configure(applicationContext)
            Log.i(TAG, "Initialized Amplify")

        } catch (e: Amplify.AlreadyConfiguredException) {
            Log.e(TAG, "Amplify already initialized", e)
        } catch (e: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", e)
        }

        return this
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun sendJson(jsonObject: JSONObject) : String {
        // Create JSON using JSONObject
//        val jsonObject = JSONObject()
//        jsonObject.put("name", "Valerio")
//        jsonObject.put("salary", "3540")
//        jsonObject.put("age", "23")

        // Convert JSONObject to String
        val jsonObjectString = jsonObject.toString()


        //GlobalScope.launch() {

            val urlString = "18.222.222.141:8080"
            val url = URL("http://$urlString")

            val clientSocket: Socket
            val port = 8080
            val socketIn: BufferedReader
            val socketOut: PrintWriter

            val httpsURLConnection = url.openConnection() as HttpURLConnection
            httpsURLConnection.requestMethod = "POST"
            httpsURLConnection.setRequestProperty(
                "Content-Type",
                "application/json"
            ) // The format of the content we're sending to the server
            httpsURLConnection.setRequestProperty(
                "Accept",
                "application/json"
            ) // The format of response we want to get from the server
            httpsURLConnection.doInput = true
            httpsURLConnection.doOutput = true

            // Send the JSON we created
            try {
                val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
                outputStreamWriter.write(jsonObjectString)
                outputStreamWriter.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}")
                return ""
            }

            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            val inSR = InputStreamReader(httpsURLConnection.inputStream)
            val br = BufferedReader(inSR).lines().collect(Collectors.joining())

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.i(TAG, "Nice: $responseCode")
                Log.i(TAG, "Response: $br")
            } else {
                Log.i("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }

            return br
        //}
    }

    fun uploadFile(uploadFile: File, filename : String) {
        Log.i(TAG, "About to upload ${uploadFile.absolutePath}")
        // Upload a file on the online storage.
        Amplify.Storage.uploadFile(filename, uploadFile,
            {Log.i(TAG, "Succesfully uploaded")},
            {error -> Log.e(TAG, "Upload failed: $filename; ${error.toString()}")})
    }

    fun downloadFile(): File {
        // Download a file from the online storage
        val filename = "ciambella"
        var file = File.createTempFile(filename,".png")

        Amplify.Storage.downloadFile("ciambella.png", file,
            {Log.i(TAG, "Succesfully uploaded")},
            {Log.e(TAG, "Upload failed")})

        return file
    }
}
