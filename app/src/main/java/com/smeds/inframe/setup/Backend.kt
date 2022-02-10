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
import javax.net.ssl.HttpsURLConnection


val ret: ReturnValue = ReturnValue()

object Backend {

    const val TAG = "Backend"

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

        val urlString = "3.138.157.8:8080"
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
        val t = SimpleThread(httpsURLConnection, jsonObjectString)
        t.start()
        t.join()

        Log.i(TAG, "Dopo aver inviato il file ${ret.ret} and $jsonObjectString")

        return ret.ret
        //}
    }

    fun uploadFile(uploadFile: File, filename : String) {
        Log.i(TAG, "About to upload ${uploadFile.absolutePath}")
        // Upload a file on the online storage.
        Amplify.Storage.uploadFile(filename, uploadFile,
            {Log.i(TAG, "Succesfully uploaded $filename")},
            {error -> Log.e(TAG, "Upload failed: $filename; ${error.toString()}")})
    }

    fun downloadFile(filename : String): File {
        // Download a file from the online storage
        var file = File.createTempFile(filename,".png")

        Amplify.Storage.downloadFile(filename, file,
            {Log.i(TAG, "Succesfully uploaded")},
            {Log.e(TAG, "Upload failed")})

        return file
    }
}

class SimpleThread(private val httpsURLConnection: HttpURLConnection, private val jsonObjectString: String): Thread() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun run() {
        super.run()
        try {
            Log.i(Backend.TAG, "Prim adi inviare il file")
            val outputStreamWriter = OutputStreamWriter(httpsURLConnection.outputStream)
            Log.i(Backend.TAG, "1 adi inviare il file")
            outputStreamWriter.write(jsonObjectString)
            Log.i(Backend.TAG, "2 adi inviare il file")
            outputStreamWriter.flush()
            Log.i(Backend.TAG, "3 adi inviare il file")
            // Check if the connection is successful
            val responseCode = httpsURLConnection.responseCode
            val inSR = InputStreamReader(httpsURLConnection.inputStream)
            val br = BufferedReader(inSR).lines().collect(Collectors.joining())

            ret.ret = br

            Log.i(Backend.TAG, "Ottenuto ill valore di ritorno $br")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Log.i(Backend.TAG, "Nice: $responseCode")
                Log.i(Backend.TAG, "Response: $br")

            } else {
                Log.i("HTTPURLCONNECTION_ERROR", responseCode.toString())
            }
            httpsURLConnection.disconnect()

        } catch (e: Exception) {
            Log.e(Backend.TAG, "Exception: ${e}")
        }
    }
}

class ReturnValue {
    @Volatile
    public var ret = ""
}