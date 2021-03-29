package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val application = getApplication<MyApplication>()
    val downloadClientInfo = MediatorLiveData<String>()

    fun download(id: String) = viewModelScope.launch {
        val response = application.repository.getDownloadClient(id)

        if (response.code() == 200 && response.body()!!.code == 0) {
            downloadClientInfo.value = response.body()!!.data
        } else {
            downloadClientInfo.value = "获取下载链接失败，请重试"
        }
    }


}