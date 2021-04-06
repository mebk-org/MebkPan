package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mebk.pan.application.MyApplication
import com.mebk.pan.bean.DownloadPrepareBean
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.utils.*
import com.mebk.pan.worker.DownloadWorker
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val application = application as MyApplication
    private val workManager = WorkManager.getInstance(application)
    val isFileOperator = MutableLiveData<Boolean>().also {
        it.value = false
    }
    var downloadListInfo = MutableLiveData<MutableList<DownloadPrepareBean>>()
    val checkInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()


    private val checkList = mutableListOf<DirectoryDto.Object>()
    private var downloadList = mutableListOf<DownloadPrepareBean>()

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

        downloadList = checkList.map { DownloadPrepareBean(it, "", 0) }.toMutableList()
        downloadListInfo.value = downloadList
        var pos = 0
        for (file in downloadList) {
            //获取下载链接
            val pair = application.repository.getDownloadClient(file.file.id)
            if (pair.first != RetrofitClient.REQUEST_SUCCESS) {
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                downloadListInfo.value = downloadList
                ++pos
                continue
            }
            file.client = pair.second
            downloadList[pos].state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
            downloadListInfo.value = downloadList
            val dataBuilder = Data.Builder()
                    .putString(DOWNLOAD_KEY_INPUT_FILE_CLIENT, file.client!!)
                    .putString(DOWNLOAD_KEY_OUTPUT_FILE_NAME, file.file.name)
                    .putLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, file.file.size)
                    .build()
            val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                    .setInputData(dataBuilder)
                    .build()
            workManager.enqueue(downloadRequest)
            downloadList[pos].state = RetrofitClient.DOWNLOAD_STATE_DONE
            downloadListInfo.value = downloadList
            ++pos
        }
    }


}