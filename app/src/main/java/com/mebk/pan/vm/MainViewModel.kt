package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*
import com.mebk.pan.worker.DownloadWorker
import kotlinx.android.synthetic.main.rv_item_history_download_waiting.view.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    var workManager: WorkManager = WorkManager.getInstance(application)

    init {
        workManager.pruneWork()
    }

    private val myApplication = application as MyApplication
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    var downloadListInfo = MutableLiveData<MutableList<DownloadingInfo>>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    var currentPos = 0
    private val channel = Channel<DownloadingInfo>()
    val downloadingList = myApplication.repository.getDownloadingInfo().asLiveData()
    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadingInfo>()
    var isDownloadDone = false
    var isDownloading = false
    var downloadWorkerInfo = MutableLiveData<WorkInfo>()

    val observer = Observer<WorkInfo> {
        downloadWorkerInfo.value = it
    }

    fun changeFileOperator() {
        isFileOperator.value = !(isFileOperator.value)!!
        checkList.clear()
        checkInfo.value = checkList
    }

    private val workerIdList = mutableListOf<UUID>()
    fun addCheck(file: DirectoryDto.Object) {
        checkList += file
        checkInfo.value = checkList
    }

    fun removeCheck(file: DirectoryDto.Object) {
        checkList -= file
        checkInfo.value = checkList
    }

    fun download() = viewModelScope.launch {

        downloadList = checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, ToolUtils.utcToLocal(it.date, ToolUtils.DATE_TYPE_UTC).time, RetrofitClient.DOWNLOAD_STATE_WAIT, 0, "") }.toMutableList()

        downloadList.forEach {
            myApplication.repository.addDownloadingInfo(it)
        }
        getDownloadClient()

        updateDownloadList()


    }

    /**
     * 更新下载列表
     */
    private suspend fun updateDownloadList() = viewModelScope.launch {
        for (file in channel) {
            myApplication.repository.updateDownloadingInfo(file)
            downloadFile(file)
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

    /**
     * 下载文件
     * @param file DownloadingInfo
     * @return Job
     */
    fun downloadFile(file: DownloadingInfo) = viewModelScope.launch {
        when (file.state) {
            RetrofitClient.DOWNLOAD_STATE_PREPARE -> {
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

                if (!isDownloading) {
                    workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever(observer)
                    isDownloading = true
                }
                workManager.enqueueUniqueWork(DOWNLOAD_KEY_OUTPUT_FILE, ExistingWorkPolicy.APPEND_OR_REPLACE, downloadRequest)
                file.state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
                file.workID = downloadRequest.id.toString()

                myApplication.repository.updateDownloadingInfo(file)
            }
            RetrofitClient.DOWNLOAD_STATE_ERR -> {
                ++currentPos
            }
        }

    }

    fun downloadDone(state: WorkInfo.State) = viewModelScope.launch {
        when (state) {
            WorkInfo.State.SUCCEEDED -> {
                if (currentPos < downloadingList.value?.size!!) {
                    downloadingList.value?.get(currentPos)?.let {
                        if (currentPos + 1 < downloadingList.value?.size!!) {
                            workManager.getWorkInfoByIdLiveData(UUID.fromString(downloadingList.value!![currentPos + 1].workID)).observeForever(observer)
                        }
                        myApplication.repository.deleteDownloadingInfo(it)
                    }
                }
            }
            WorkInfo.State.CANCELLED -> {
                isDownloading = false
                ++currentPos
                if (currentPos >= downloadingList.value!!.size) isDownloadDone = true
            }
            WorkInfo.State.FAILED -> {
                isDownloading = false
                ++currentPos
                if (currentPos >= downloadingList.value!!.size) isDownloadDone = true
            }
        }

    }

}