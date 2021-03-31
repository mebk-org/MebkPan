package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.launch

class DownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val application = getApplication<MyApplication>()
    val downloadClientInfo = MutableLiveData<String>()

    fun download(id: String) = viewModelScope.launch {
        val pair = application.repository.getDownloadClient(id)

        if (pair.first == RetrofitClient.REQUEST_SUCCESS) {
            downloadClientInfo.value = pair.second
        } else {
            downloadClientInfo.value = "获取下载链接失败，请重试"
        }
    }


}