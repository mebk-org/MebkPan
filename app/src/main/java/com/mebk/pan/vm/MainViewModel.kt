package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.NIOUtils
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val DOWNLOAD_STATE_WAIT = 0
        const val DOWNLOAD_STATE_PREPARE = 1
        const val DOWNLOAD_STATE_DONE = 2
        const val DOWNLOAD_STATE_ERR = 3
    }

    data class DownloadPrepare(
            val file: DirectoryDto.Object,
            var client: String?,
            var state: Int?
    )

    private val application = application as MyApplication

    var downloadClientInfo = MutableLiveData<String>()
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }

    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadPrepare>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    fun changeFileOperator() {
        isFileOperator.value = !(isFileOperator.value)!!
        checkList.clear()
        checkInfo.value = checkList
    }

    fun addCheck(file: DirectoryDto.Object) {
        checkList += file
        checkInfo.value = checkList
    }

    fun removeCheck(file: DirectoryDto.Object) {
        checkList -= file
        checkInfo.value = checkList
    }

    fun download() = viewModelScope.launch {
        downloadList = checkList.map { DownloadPrepare(it, "", 0) }.toMutableList()

        for (file in downloadList) {
            //获取下载链接
            val pair = application.repository.getDownloadClient(file.file.id)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = DOWNLOAD_STATE_ERR
                continue
            }
            file.client = pair.second

            writeFile(file.client!!, file.file.name)
        }
    }


    private fun writeFile(client: String, name: String) = viewModelScope.launch {
        val responseBody = application.repository.downloadFile(client)
        val nio = NIOUtils(MyApplication.path!! + name)
        with(responseBody.byteStream()) {
            val byteArray = kotlin.ByteArray(4096)
            while (read(byteArray, 0, 4096) != -1) {
                nio.write(byteArray)
            }
            LogUtil.err(FileInfoViewModel::class.java, "下载完成")
            nio.close()
        }
    }


}