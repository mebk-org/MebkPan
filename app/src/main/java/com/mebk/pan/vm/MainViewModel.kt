package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.bean.DownloadPrepareBean
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.NIOUtils
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
import kotlinx.coroutines.launch
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {


    private val application = application as MyApplication

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

            writeFile(file.client!!, file.file.name)

            downloadList[pos].state = RetrofitClient.DOWNLOAD_STATE_DONE
            downloadListInfo.value = downloadList
            ++pos
        }
    }


    private fun writeFile(client: String, name: String) = viewModelScope.launch {
        val responseBody = application.repository.downloadFile(client)
        val nio = NIOUtils(MyApplication.path!! + name)
        with(responseBody.byteStream()) {
            val byteArray = kotlin.ByteArray(4096)
            try {
                while (read(byteArray, 0, 4096) != -1) {
                    nio.write(byteArray)
                }
            } catch (e: IOException) {
                LogUtil.err(FileInfoViewModel::class.java, e.toString())
            }

            LogUtil.err(FileInfoViewModel::class.java, "下载完成")
            nio.close()
        }
    }


}