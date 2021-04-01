package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
     val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }

    fun changeFileOperator() {
        isFileOperator.value = !(isFileOperator.value)!!
    }


}