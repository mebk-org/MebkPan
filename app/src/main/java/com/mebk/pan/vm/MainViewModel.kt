package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*
import com.mebk.pan.worker.DownloadWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val myApplication = application as MyApplication
    private val workManager = WorkManager.getInstance(application)
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    var downloadListInfo = MutableLiveData<MutableList<DownloadingInfo>>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    val downloadWorkInfo = workManager.getWorkInfosByTagLiveData(DOWNLOAD_TAG)
    var currentPos = 0
    private val channel = Channel<DownloadingInfo>()
    private val downloadClientList = mutableListOf<DownloadingInfo>()
    val downloadingList = myApplication.repository.getDownloadingInfo().asLiveData()
    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadingInfo>()
    var isDownloadingDone = false
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

        downloadList = checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, ToolUtils.utcToLocal(it.date, ToolUtils.DATE_TYPE_UTC).time, RetrofitClient.DOWNLOAD_STATE_WAIT, 0) }.toMutableList()

        downloadList.forEach {
            myApplication.repository.addDownloadingInfo(it)
        }
        getDownloadClient()

        updateDownloadList()


    }

    /**
     * 更新下载列表
     */
    private suspend fun updateDownloadList() {
        for (file in channel) {
            myApplication.repository.updateDownloadingInfo(file)
        }
    }

    /**
     * 获取下载链接
     * @return Job
     */
    private suspend fun getDownloadClient() = viewModelScope.launch {
        var pos = 0
        for (file in downloadList) {

            val pair = myApplication.repository.getDownloadClient(file.id)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                myApplication.repository.updateDownloadingInfo(file)
                ++pos
                continue
            }

            file.client = pair.second
            file.state = RetrofitClient.DOWNLOAD_STATE_PREPARE
            myApplication.repository.updateDownloadingInfo(file)
            channel.send(file)
        }
    }

    fun downloadFile(file: DownloadingInfo) = viewModelScope.launch {
        val dataBuilder = Data.Builder()
                .putString(DOWNLOAD_KEY_OUTPUT_FILE_ID, file.id)
                .putString(DOWNLOAD_KEY_OUTPUT_FILE_NAME, file.name)
                .putString(DOWNLOAD_KEY_INPUT_FILE_CLIENT, file.client)
                .putLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, file.size)
                .putLong(DOWNLOAD_KEY_OUTPUT_FILE_DATE, file.date)
                .putString(DOWNLOAD_KEY_OUTPUT_FILE_TYPE, file.type)
                .build()
        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(dataBuilder)
                .addTag(DOWNLOAD_TAG)
                .build()
        val requestId = downloadRequest.id
        workManager.enqueueUniqueWork(DOWNLOAD_KEY_OUTPUT_FILE, ExistingWorkPolicy.APPEND_OR_REPLACE, downloadRequest)
    }

    fun downloadDone() = viewModelScope.launch {
        if (downloadingList.value!!.size > 1) downloadFile(downloadingList.value!![1])
        myApplication.repository.deleteDownloadingInfo(downloadingList.value!![0])
    }

    fun workerFinish(uuid: UUID) {
        workManager.cancelWorkById(uuid)
    }

}