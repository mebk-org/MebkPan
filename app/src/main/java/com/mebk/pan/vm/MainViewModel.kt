package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mebk.pan.dtos.DirectoryDto

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }

    private val checkList = mutableListOf<DirectoryDto.Object>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    fun changeFileOperator() {
        isFileOperator.value = !(isFileOperator.value)!!
    }

    fun addCheck(file: DirectoryDto.Object) {
        checkList += file
        checkInfo.value = checkList
    }

    fun removeCheck(file: DirectoryDto.Object) {
        checkList -= file
        checkInfo.value = checkList
    }

}