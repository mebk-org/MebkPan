package com.mebk.pan.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
     val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }

    fun changeFileOperator() {
        isFileOperator.value = !(isFileOperator.value)!!
    }


}