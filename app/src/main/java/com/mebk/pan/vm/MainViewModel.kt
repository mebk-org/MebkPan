package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.*
import com.mebk.pan.worker.DownloadWorker
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private var workManager: WorkManager = WorkManager.getInstance(application)
    private val myApplication = application as MyApplication
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    var checkPath: String = ""
    private var downloadList = mutableListOf<DownloadingInfo>()
    private var queueList = mutableListOf<String>()
    private val historyDownloadIdList = mutableListOf<String>()
    val checkInfo = MutableLiveData<MutableList<File>>()
    private val downloadChannel = Channel<DownloadingInfo>(100)
    private val clientChannel = Channel<DownloadingInfo>(100)
    val checkList = mutableListOf<File>()
    private val workerIdList = mutableListOf<Pair<String, UUID>>()
    private var isDownloadDone = false
    private var isDownloading = false
    var downloadWorkerInfo = MutableLiveData<WorkInfo>()
    private var cancelList = mutableListOf<String>()

    private var failedCount = 0
    private var successCount = 0
    private var cancelCount = 0

    companion object {
        val ACTION_START = 0
        val ACTION_DONE = 1
    }

    var actionInfo = MutableLiveData<Int>()

    init {
        workManager.pruneWork()
        viewModelScope.launch {
            queueList = myApplication.repository.getDownloadingFileIdList().toMutableList()
            if (queueList.isNotEmpty()) {
                workerIdList.addAll(queueList.zip(
                        myApplication.repository.getDownloadingWorkIdList().map {
                            UUID.fromString(it)
                        }).toMutableList())
                workManager.getWorkInfoByIdLiveData(workerIdList[0].second).observeForever(observer)
                isDownloading = true
                isDownloadDone = false
            }
        }
    }

    private val observer = Observer<WorkInfo> {
        downloadWorkerInfo.value = it
    }

    /**
     * 变更文件操作页面
     * @param path String 当前path(只有进入到文件选择页面才需要设置)
     */
    fun changeFileOperator(path: String = "") {
        LogUtil.err(this.javaClass, "${isFileOperator.value}")
        isFileOperator.value = !(isFileOperator.value)!!
        checkList.clear()
        checkInfo.value = checkList
        checkPath = path
    }

    fun addCheck(file: File) {
        checkList += file
        checkInfo.value = checkList
    }

    fun removeCheck(file: File) {
        checkList -= file
        checkInfo.value = checkList
    }

    /**
     * 下载文件
     * @return Job
     */
    fun download() = viewModelScope.launch {
        isDownloadDone = false
        isDownloading = false
        successCount = 0
        failedCount = 0
        downloadList.clear()

        if (historyDownloadIdList.isEmpty()) {
            historyDownloadIdList.addAll(checkList.map { it.id })
            downloadList.addAll(checkList.map { DownloadingInfo(it.id, it.name, "", "", it.size, it.type, utcToLocal(it.date, DATE_TYPE_UTC).time, DOWNLOAD_STATE_WAIT, 0, "") })
        } else {
            for (file in checkList) {
                if (historyDownloadIdList.indexOf(file.id) == -1) {
                    downloadList.add(DownloadingInfo(file.id, file.name, "", "", file.size, file.type, utcToLocal(file.date, DATE_TYPE_UTC).time, DOWNLOAD_STATE_WAIT, 0, ""))
                    historyDownloadIdList.add(file.id)
                }
            }
        }


        for (file in downloadList) {
            myApplication.repository.addDownloadingInfo(file)
            clientChannel.send(file)
        }
        getDownloadClient()

        updateDownloadList()

    }

    /**
     * 更新下载列表
     */
    private suspend fun updateDownloadList() = viewModelScope.launch {
        for (file in downloadChannel) {
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
        for (file in clientChannel) {
            if (cancelList.indexOf(file.fileId) != -1) {
                myApplication.repository.updateDownloadingState(file.fileId, DOWNLOAD_STATE_CANCEL)
                downloadList.removeAt(pos)
            } else {
                val pair = myApplication.repository.getDownloadClient(file.fileId)
                if (pair.first != REQUEST_SUCCESS) {
                    file.state = DOWNLOAD_STATE_CLIENT_ERR
                    myApplication.repository.updateDownloadingState(file.fileId, DOWNLOAD_STATE_CLIENT_ERR)
                    ++pos
                    continue
                }
                file.client = pair.second
                myApplication.repository.updateDownloadingClient(file.fileId, pair.second)
                downloadChannel.send(file)
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
        myApplication.repository.updateDownloadingState(file.fileId, DOWNLOAD_STATE_PREPARE)
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
        myApplication.repository.updateDownloadingDate(queueList[successCount + failedCount], System.currentTimeMillis() / 1000)
        myApplication.repository.updateDownloadFilePath(queueList[successCount + failedCount], MyApplication.path!!)
        myApplication.repository.updateDownloadingState(queueList[successCount + failedCount], changeState(state))
        if (failedCount + successCount + 1 >= queueList.size) {
            isDownloadDone = true
            isDownloading = false
            failedCount = 0
            successCount = 0
            cancelCount = 0
            queueList.clear()
            workerIdList.clear()
        }
        LogUtil.err(this@MainViewModel.javaClass, "success=$successCount,failed=$failedCount")
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
            WorkInfo.State.BLOCKED -> DOWNLOAD_STATE_WAIT
            WorkInfo.State.ENQUEUED -> DOWNLOAD_STATE_WAIT
            WorkInfo.State.RUNNING -> DOWNLOAD_STATE_DOWNLOADING
            WorkInfo.State.SUCCEEDED -> DOWNLOAD_STATE_DONE
            WorkInfo.State.FAILED -> DOWNLOAD_STATE_DOWNLOAD_ERR
            WorkInfo.State.CANCELLED -> DOWNLOAD_STATE_CANCEL
        }
    }

    /**
     * 取消下载
     * @param id String 取消的文件id
     * @return Job
     */
    fun cancelDownload(id: String) = viewModelScope.launch {
        cancelList.add(id)
        cancelCount++
        myApplication.repository.updateDownloadingState(id, DOWNLOAD_STATE_CANCEL)
        myApplication.repository.updateDownloadingDate(id, System.currentTimeMillis())
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

    /**
     *  删除文件
     * @return Job
     */
    fun deleteFile() = viewModelScope.launch {
        actionInfo.value = ACTION_START
        var fileList = mutableListOf<String>()
        var dirList = mutableListOf<String>()
        var pathSet = mutableSetOf<String>()
        checkList.forEach {
            if (it.type == "file") {
                fileList.add(it.id)
            } else if (it.type == "dir") {
                dirList.add(it.id)
                val url = if (it.path != "/") {
                    "${it.path}/${it.name}"
                } else {
                    it.path + it.name
                }
                pathSet.add(url)
            }
        }
        val pair = myApplication.repository.deleteFile(Pair(fileList, dirList), pathSet.toList())
        if (pair.first == REQUEST_SUCCESS) {
            actionInfo.value = ACTION_DONE
        }
    }

    fun actionDone() {
        actionInfo.value = ACTION_DONE
    }

    /**
     * 文件分享
     * @param id String 文件id
     * @param isDir Boolean 是否为文件夹
     * @param pwd String 分享密码
     * @param downloads Int 下载次数，-1为不限
     * @param expire Long 有效期 单位秒
     * @param preview Boolean 是否支持预览
     * @param score Int 积分，暂未开通默认为0
     * @return Job
     */
    fun shareFile(id: String, isDir: Boolean, pwd: String, downloads: Int, expire: Long, preview: Boolean, score: Int = 0) = viewModelScope.launch {
        val pair = myApplication.repository.shareFile(id, isDir, pwd, downloads, expire, preview, score)

    }
}