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
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var workManager: WorkManager = WorkManager.getInstance(application)
    private val myApplication = application as MyApplication
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    private var downloadList = mutableListOf<DownloadingInfo>()
    private var queueList = mutableListOf<String>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()
    private val channel = Channel<DownloadingInfo>()
    private val checkList = mutableListOf<DirectoryDto.Object>()
    private val workerIdList = mutableListOf<Pair<String, UUID>>()
    private var isDownloadDone = false
    private var isDownloading = false
    var downloadWorkerInfo = MutableLiveData<WorkInfo>()
    private var cancelList = mutableListOf<String>()

    private var totalCount = 0
    private var failedCount = 0
    private var successCount = 0
    private var cancelCount = 0

    init {
        workManager.pruneWork()
        viewModelScope.launch {
            downloadList = myApplication.repository.getDownloadPrepareList().toMutableList()
            if (downloadList.isNotEmpty()) {
                totalCount = downloadList.size
                workManager.getWorkInfoByIdLiveData(UUID.fromString(downloadList[0].workID)).observeForever(observer)
                isDownloading = true
                isDownloadDone = false
            }
        }
    }

    private val observer = Observer<WorkInfo> {
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
        var pos = 0
        isDownloadDone = false
        isDownloading = false
        successCount = 0
        failedCount = 0
        if (downloadList.isNotEmpty()) {
            pos = downloadList.size
            downloadList.addAll(checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, ToolUtils.utcToLocal(it.date, ToolUtils.DATE_TYPE_UTC).time, RetrofitClient.DOWNLOAD_STATE_WAIT, 0, "") })
        } else {
            downloadList = checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, ToolUtils.utcToLocal(it.date, ToolUtils.DATE_TYPE_UTC).time, RetrofitClient.DOWNLOAD_STATE_WAIT, 0, "") }.toMutableList()
        }
        totalCount = downloadList.size
        for (i in pos until downloadList.size) {
            myApplication.repository.addDownloadingInfo(downloadList[i])
        }
        getDownloadClient()

        updateDownloadList()

    }

    /**
     * 更新下载列表
     */
    private suspend fun updateDownloadList() = viewModelScope.launch {
        for (file in channel) {
            if (cancelList.indexOf(file.fileId) != -1) continue
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
            if (cancelList.indexOf(file.fileId) != -1) {
                myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_CANCEL)
                downloadList.removeAt(pos)
                --totalCount
            } else {
                val pair = myApplication.repository.getDownloadClient(file.fileId)
                if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                    file.state = RetrofitClient.DOWNLOAD_STATE_CLIENT_ERR
                    myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_CLIENT_ERR)
                    ++pos
                    continue
                }
                file.client = pair.second
                myApplication.repository.updateDownloadingClient(file.fileId, pair.second)
                channel.send(file)
                ++pos
            }
        }
    }

    /**
     * 下载文件
     * @param file DownloadingInfo
     * @return Job
     */
    private fun downloadFile(file: DownloadingInfo) = viewModelScope.launch {
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
                .addTag(file.fileId)
                .build()
        workerIdList.add(Pair(file.fileId, downloadRequest.id))
        if (!isDownloading) {
            workManager.getWorkInfoByIdLiveData(downloadRequest.id).observeForever(observer)
            isDownloading = true
        }
        workManager.enqueueUniqueWork(DOWNLOAD_KEY_OUTPUT_FILE, ExistingWorkPolicy.APPEND_OR_REPLACE, downloadRequest)
        queueList.add(file.fileId)
        myApplication.repository.updateDownloadingState(file.fileId, RetrofitClient.DOWNLOAD_STATE_PREPARE)
        myApplication.repository.updateDownloadingWorkId(file.fileId, downloadRequest.id.toString())
        LogUtil.err(this@MainViewModel.javaClass, "queuelistsize=${queueList.size}")
    }


    /**
     * 下载完成回调
     * @param state State
     * @return Job
     */
    fun downloadDone(state: WorkInfo.State) = viewModelScope.launch {
        LogUtil.err(this@MainViewModel.javaClass, "info=${state}")
        myApplication.repository.updateDownloadingState(queueList[successCount + failedCount], changeState(state))
        if (failedCount + successCount + 1 >= queueList.size) {
            isDownloadDone = true
            isDownloading = false
            totalCount = 0
            failedCount = 0
            successCount = 0
            cancelCount = 0
            downloadList.clear()
        }
        LogUtil.err(this@MainViewModel.javaClass, "total=$totalCount,success=$successCount,failed=$failedCount")
        when (state) {
            WorkInfo.State.SUCCEEDED -> {
                ++successCount
                if (!isDownloadDone) {
                    workManager.getWorkInfoByIdLiveData(workerIdList[successCount + failedCount].second).observeForever(observer)
                }
            }
            WorkInfo.State.CANCELLED -> {
                ++failedCount
            }
            WorkInfo.State.FAILED -> {
                ++failedCount
            }
            else -> {
            }
        }
    }

    /**
     * 获取正在下载的索引
     * 当前索引为 成功数+失败数
     * @return Int
     */
    fun getCurrentPos() = successCount + failedCount

    /**
     * 转换workinfo state
     * @param state State
     * @return Int
     */
    private fun changeState(state: WorkInfo.State): Int {
        return when (state) {
            WorkInfo.State.BLOCKED -> RetrofitClient.DOWNLOAD_STATE_WAIT
            WorkInfo.State.ENQUEUED -> RetrofitClient.DOWNLOAD_STATE_WAIT
            WorkInfo.State.RUNNING -> RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
            WorkInfo.State.SUCCEEDED -> RetrofitClient.DOWNLOAD_STATE_DONE
            WorkInfo.State.FAILED -> RetrofitClient.DOWNLOAD_STATE_DOWNLOAD_ERR
            WorkInfo.State.CANCELLED -> RetrofitClient.DOWNLOAD_STATE_CANCEL
        }
    }

    fun cancelDownload(id: String) = viewModelScope.launch {
        cancelList.add(id)
        cancelCount++
        myApplication.repository.updateDownloadingState(id, RetrofitClient.DOWNLOAD_STATE_CANCEL)
        val pos = queueList.indexOf(id)
        if (pos != -1) {
            queueList.removeAt(pos)
            var workPos = 0
            for (pair in workerIdList) {
                if (pair.first == id) {
                    workManager.cancelWorkById(pair.second)
                    workerIdList.removeAt(workPos)
                    break
                }
                ++workPos
            }
        }

    }
}