package com.example.livedatademo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

class TestViewModel : ViewModel() {

    val user = liveData<String> {
        emit(System.currentTimeMillis().toString() + "qwe")
    }

}
