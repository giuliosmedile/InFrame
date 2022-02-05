package com.smeds.inframe.model

class User(username: String, email: String) {
    var username : String = username
        private set
    var email : String = email
        private set
    var devices : MutableList<Device>
        private set

    init {
        devices = mutableListOf()
    }

}