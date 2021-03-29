package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch
import java.util.*

class InternalFileViewModel(application: Application) : AndroidViewModel(application) {


    val application = getApplication<MyApplication>()
    val requestInfo = MutableLiveData<String>().also { it.value = "获取失败" }
    var fileList = mutableListOf<DirectoryDto.Object>()

    var flieInfo = MutableLiveData<MutableList<DirectoryDto.Object>>()

    private val fileStack = Stack<Pair<String, MutableList<DirectoryDto.Object>>>()
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

        val response = application.repository.getInternalFile(url)
        if (response.code() == 200) {
            requestInfo.value = "获取成功"
            if (response.body()!!.code == 0) {
                LogUtil.err(this.javaClass, response.body().toString())
                fileList = response.body()!!.data.objects as MutableList<DirectoryDto.Object>
                flieInfo.value = fileList
                fileStack.push(Pair(name, fileList.toMutableList()))
                stackSize.value = fileStack.size
            }
        } else {
            requestInfo.value = response.body()!!.msg
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
            flieInfo.value = fileList
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