package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*
import com.mebk.pan.worker.DownloadWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val application = application as MyApplication
    private val workManager = WorkManager.getInstance(application)
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    var downloadListInfo = MutableLiveData<MutableList<DownloadInfo>>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    private val channel = Channel<DownloadInfo>()


    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadInfo>()

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

        downloadList = checkList.map { DownloadInfo(it.id, it.name, "", "", it.size, it.type, 0L, RetrofitClient.DOWNLOAD_STATE_WAIT) }.toMutableList()

        for (file in downloadList) {
            application.repository.addDownloadInfo(file)
        }

        getDownloadClient()

        downloadFile()

    }

    //获取下载链接
    private suspend fun getDownloadClient() = viewModelScope.launch {
        var pos = 0
        for (file in downloadList) {

            val pair = application.repository.getDownloadClient(file.id)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                application.repository.updateDownloadInfo(file)
                ++pos
                continue
            }

            file.client = pair.second
            file.state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
            channel.send(file)
            application.repository.updateDownloadInfo(file)
        }
    }

    private suspend fun downloadFile() = viewModelScope.launch {
        for (file in channel) {
            val dataBuilder = Data.Builder()
                    .putString(DOWNLOAD_KEY_INPUT_FILE_CLIENT, file.client!!)
                    .putString(DOWNLOAD_KEY_OUTPUT_FILE_NAME, file.name)
                    .putLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, file.size)
                    .build()
            val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(dataBuilder)
                    .build()
            workManager.enqueue(downloadRequest)
        }
    }
}