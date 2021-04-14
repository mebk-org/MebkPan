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
    private var downloadListInfo = mutableListOf<DownloadingInfo>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    private val channel = Channel<DownloadingInfo>()
    private val checkList = mutableListOf<DirectoryDto.Object>()
    private val workerIdList = mutableListOf<UUID>()
    var isDownloadDone = false
    var isDownloading = false
    var downloadWorkerInfo = MutableLiveData<WorkInfo>()
    private var totalCount = 0
    private var failedCount = 0
    private var successCount = 0

    val observer = Observer<WorkInfo> {
        downloadWorkerInfo.value = it
    }

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

    /**
     * 下载文件
     * @return Job
     */
    fun download() = viewModelScope.launch {
        downloadListInfo.clear()
        downloadListInfo = checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, ToolUtils.utcToLocal(it.date, ToolUtils.DATE_TYPE_UTC).time, RetrofitClient.DOWNLOAD_STATE_WAIT, 0, "") }.toMutableList()
        totalCount = downloadListInfo.size
        downloadListInfo.forEach {
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
            downloadFile(file)
        }
    }

    /**
     * 获取下载链接
     * @return Job
     */
    private suspend fun getDownloadClient() = viewModelScope.launch {
        var pos = 0
        for (file in downloadListInfo) {
            val pair = myApplication.repository.getDownloadClient(file.fileId)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = RetrofitClient.DOWNLOAD_STATE_CLIENT_ERR
                myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_CLIENT_ERR)
                ++pos
                continue
            }

            myApplication.repository.updateDownloadingClient(file.fileId, pair.second)
            myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_PREPARE)
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
                        .putString(DOWNLOAD_KEY_OUTPUT_FILE_ID, file.fileId)
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
                workerIdList.add(downloadRequest.id)
                if (!isDownloading) {
                    workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever(observer)
                    isDownloading = true
                }
                workManager.enqueueUniqueWork(DOWNLOAD_KEY_OUTPUT_FILE, ExistingWorkPolicy.APPEND_OR_REPLACE, downloadRequest)
                myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_WAIT)
                myApplication.repository.updateDownloadingWorkId(file.fileId, downloadRequest.id.toString())
            }
            RetrofitClient.DOWNLOAD_STATE_ERR -> {
                ++failedCount
            }
        }

    }

    /**
     * 下载完成回调
     * @param state State
     * @return Job
     */
    fun downloadDone(state: WorkInfo.State) = viewModelScope.launch {

        when (state) {
            WorkInfo.State.SUCCEEDED -> {
                ++successCount
                if (failedCount + successCount >= totalCount) isDownloadDone = true
                if (!isDownloadDone) {
                    workManager.getWorkInfoByIdLiveData(workerIdList[successCount + failedCount]).observeForever(observer)
                    myApplication.repository.updateDownloadingState(downloadListInfo[successCount + failedCount].fileId, RetrofitClient.DOWNLOAD_STATE_DONE)
                }
            }
            WorkInfo.State.CANCELLED -> {
                ++failedCount
                if (failedCount + successCount >= totalCount) isDownloadDone = true
            }
            WorkInfo.State.FAILED -> {
                ++failedCount
                if (failedCount + successCount >= totalCount) isDownloadDone = true
            }
        }
    }

    /**
     * 获取正在下载的索引
     * 当前索引为 成功数+失败数
     * @return Int
     */
    fun getCurrentPos() = successCount + failedCount
}