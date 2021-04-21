package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.launch
import java.util.*

class InternalFileViewModel(application: Application) : AndroidViewModel(application) {


    val application = getApplication<MyApplication>()
    val requestInfo = MutableLiveData<String>().also { it.value = "获取失败" }
    var fileList = mutableListOf<File>()
    var fileInfo = MutableLiveData<MutableList<File>>()

    private val fileStack = Stack<Pair<String, MutableList<File>>>()
    var stackSize = MutableLiveData<Int>().also {
        it.value = fileStack.size
    }

    //获取文件夹下内容
    fun internalFile(name: String, path: String = "/") = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }

        LogUtil.err(this.javaClass, url)

        val pair = application.repository.getInternalFile(url)
        if (pair.first == RetrofitClient.REQUEST_SUCCESS) {
            requestInfo.value = "获取成功"
            LogUtil.err(this.javaClass, pair.second.toString())

            fileList = pair.second!!.objects as MutableList<File>
            fileInfo.value = fileList


            fileStack.push(Pair(name, fileList.toMutableList()))
            stackSize.value = fileStack.size
        } else {
            requestInfo.value = pair.first
        }
    }


    /**
     * 返回事件
     * @return Boolean 是否为最顶层文件夹
     */
    fun back(): Boolean {
        return if (fileStack.size > 1) {
            fileStack.pop()
            stackSize.value = fileStack.size
            fileList = fileStack.peek().second
            fileInfo.value = fileList
            false
        } else {
            if (!fileStack.empty()) {
                fileStack.pop()
                stackSize.value = fileStack.size
            }
            false
        }

    }
}