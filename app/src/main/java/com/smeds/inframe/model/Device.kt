package com.smeds.inframe.model

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.smeds.inframe.data.DeviceInfo
import org.json.JSONObject
import android.provider.Settings.Secure




class Device() {

    lateinit var context: Context
    lateinit var windowManager : WindowManager
    lateinit var user : User
    lateinit var name : String
    lateinit var deviceInfo: DeviceInfo
    lateinit var android_id : String


    constructor(user : User, windowManager: WindowManager, context : Context) : this() {
        this.user = user
        this.windowManager = windowManager
        this.context = context

        android_id = Secure.getString(
            context.contentResolver,
            Secure.ANDROID_ID
        )
        name = Build.MANUFACTURER + "-" + Build.MODEL + "-" + android_id
        deviceInfo = DeviceInfo(windowManager)
    }

    constructor(json : JSONObject) : this() {
        // Fill the deviceinfo
        var screenInches : Double = json.getDouble("screenInches")
        var screenWidthPx : Int = json.getInt("screenWidthPx")
        var screenHeightPx : Int = json.getInt("screenHeightPx")
        var screenWidthDp : Int = json.getInt("screenWidthDp")
        var screenHeightDp : Int = json.getInt("screenHeightDp")
        var screenWidthInch : Double = json.getDouble("screenWidthInch")
        var screenHeightInch : Double = json.getDouble("screenHeightInch")
        var density : Int = json.getInt("density")
        name = json.getString("deviceName")
        user = User(json.getString("user"), "")
        deviceInfo = DeviceInfo(screenInches, screenWidthPx, screenHeightPx, screenWidthDp, screenHeightDp, screenWidthInch, screenHeightInch, density)

    }


    fun toJSONObject() : JSONObject {
        val json = deviceInfo.toJsonObject()
        json.put("deviceName", name)
        json.put("user", user.username)
        return json
    }

    override fun toString(): String {
        return "{name: $name, user: $user, deviceInfo: ${deviceInfo}}"
    }



}