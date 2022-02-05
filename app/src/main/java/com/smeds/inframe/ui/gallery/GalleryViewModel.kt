package com.smeds.inframe.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smeds.inframe.model.Device
import com.smeds.inframe.model.User

class GalleryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is gallery Fragment"
    }
    val text: LiveData<String> = _text

    private val _recyclerView = MutableLiveData<List<Device>>().apply {
        value = null
    }
    val recyclerView : LiveData<List<Device>> = _recyclerView
}