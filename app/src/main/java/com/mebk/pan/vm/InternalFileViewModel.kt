package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch

class InternalFileViewModel(application: Application) : AndroidViewModel(application) {
    val application = getApplication<MyApplication>()
    val requestInfo = MutableLiveData<String>().also { it.value = "获取失败" }
    var fileList = mutableListOf<DirectoryDto.Object>()

    var flieInfo = MutableLiveData<MutableList<DirectoryDto.Object>>().also {}

    //获取文件夹下内容
    fun internalFile(name: String, path: String = "/") = viewModelScope.launch {

        val path = path + name
        LogUtil.err(this.javaClass, path)

        val response = application.repository.getInternalFile(path)
        if (response.code() == 200) {
            requestInfo.value = "获取成功"
            if (response.body()!!.code == 0) {
                LogUtil.err(this.javaClass, response.body().toString())
                fileList = response.body()!!.data.objects as MutableList<DirectoryDto.Object>
                flieInfo.value = fileList
            }
        } else {
            requestInfo.value = response.body()!!.msg
        }
    }
}