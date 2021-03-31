package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.NIOUtils
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.launch

class FileInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val application = getApplication<MyApplication>()
    val downloadClientInfo = MutableLiveData<String>()
    val fileInfo = MutableLiveData<FileInfoDto>()

    fun getDownloadClient(id: String) = viewModelScope.launch {
        val pair = application.repository.getDownloadClient(id)

        if (pair.first == RetrofitClient.REQUEST_SUCCESS) {
            downloadClientInfo.value = pair.second
        } else {
            downloadClientInfo.value = "获取下载链接失败，请重试"
        }
    }

    fun writeFile(path: String) = viewModelScope.launch {
        val responseBody = application.repository.downloadFile(downloadClientInfo.value!!)
        val nio = NIOUtils(path)
        with(responseBody.byteStream()) {
            val byteArray = ByteArray(4096)
            while (read(byteArray, 0, 4096) != -1) {
                nio.write(byteArray)
            }
            LogUtil.err(FileInfoViewModel::class.java, "下载完成")
            nio.close()
        }
    }

    fun getFileInfo(id: String, type: String) = viewModelScope.launch {
        val pair = application.repository.getFileInfo(id, type == "dir")
        if (pair.first == RetrofitClient.REQUEST_SUCCESS) {
            fileInfo.value = pair.second!!
        }
    }

}