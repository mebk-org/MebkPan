package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.database.entity.HistoryDownloadInfo
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
    var downloadListInfo = MutableLiveData<MutableList<DownloadingInfo>>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    val downloadWorkInfo = workManager.getWorkInfosByTagLiveData(DOWNLOAD_TAG)
    private val channel = Channel<DownloadingInfo>()


    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadingInfo>()

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
            application.repository.addDownloadingInfo(it)
        }
        getDownloadClient()

        downloadFile()

    }

    /**
     * 获取下载链接
     * @return Job
     */
    private suspend fun getDownloadClient() = viewModelScope.launch {
        var pos = 0
        for (file in downloadList) {

            val pair = application.repository.getDownloadClient(file.id)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                application.repository.updateDownloadingInfo(file)
                ++pos
                continue
            }

            file.client = pair.second
            file.state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
            application.repository.updateDownloadingInfo(file)
            channel.send(file)
        }
    }

    private suspend fun downloadFile() = viewModelScope.launch {
        for (file in channel) {
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
            workManager.enqueue(downloadRequest)
        }
    }
}