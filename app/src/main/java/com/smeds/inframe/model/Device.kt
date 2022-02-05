package com.smeds.inframe.model

import android.os.Build
import android.util.Log
import android.view.WindowManager
import com.smeds.inframe.data.DeviceInfo
import org.json.JSONObject

class Device (user : User, windowManager: WindowManager){

    companion object {
        var counter : Int = 0
    }

    var name : String = Build.MANUFACTURER + "-" + Build.MODEL + "-" + counter++
    var user : User = user
    var deviceInfo : DeviceInfo = DeviceInfo(windowManager)

    fun toJSONObject() : JSONObject {
        val json = deviceInfo.toJsonObject()
        json.put("deviceName", name)
        json.put("user", user.username)
        return json
    }



}