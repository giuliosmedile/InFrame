package com.smeds.inframe.model

class User(username: String, email: String) {
    var username : String = username
        private set
    var email : String = email
        private set
    var devices : ArrayList<Device>

    init {
        devices = ArrayList()
    }

    override fun toString(): String {
        return "username: $username, email: $email, devices: ${devices.joinToString()}"
    }

}