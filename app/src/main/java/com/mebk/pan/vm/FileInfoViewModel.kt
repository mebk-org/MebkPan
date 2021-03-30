package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.NIOUtils
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Response

class FileInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val application = getApplication<MyApplication>()
    val downloadClientInfo = MutableLiveData<String>()

    fun download(id: String) = viewModelScope.launch {
        val response = application.repository.getDownloadClient(id)

        if (response.code() == 200 && response.body()!!.code == 0) {
            downloadClientInfo.value = response.body()!!.data
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

}