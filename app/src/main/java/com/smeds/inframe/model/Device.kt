package com.smeds.inframe.model

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.smeds.inframe.data.DeviceInfo
import org.json.JSONObject
import android.provider.Settings.Secure




class Device (user : User, windowManager: WindowManager, context : Context){

    private val android_id = Secure.getString(
        context.contentResolver,
        Secure.ANDROID_ID
    )
    var name : String = Build.MANUFACTURER + "-" + Build.MODEL + "-" + android_id
    var user : User = user
    var deviceInfo : DeviceInfo = DeviceInfo(windowManager)

    fun toJSONObject() : JSONObject {
        val json = deviceInfo.toJsonObject()
        json.put("deviceName", name)
        json.put("user", user.username)
        return json
    }



}