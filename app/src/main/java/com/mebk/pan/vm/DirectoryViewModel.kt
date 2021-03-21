package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {


    private val application = getApplication<MyApplication>()

    var directoryInfo = MutableLiveData<MutableList<DirectoryDto.Object>>().also {}

    private var requestInfo = MutableLiveData<String>().also {
        it.value = "获取失败"
    }

    fun directory() = viewModelScope.launch {
        val response = application.repository.getDirectory()
        if (response.code() == 200) {
            requestInfo.value = "获取成功"
            if (response.body()!!.code == 0) {
                directoryInfo.value = response.body()!!.data.objects as MutableList
            }
        } else {
            requestInfo.value = response.body()!!.msg
        }
    }

}