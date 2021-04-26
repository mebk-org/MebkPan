package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch
import java.util.*

class DirViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    private var dirList = listOf<File>()
    var dirInfo = MutableLiveData<List<File>>()
    private val fileStack = Stack<Pair<Pair<String, String>, List<File>>>()
    var stackSize = MutableLiveData<Int>().also {
        it.value = fileStack.size
    }

    fun getDir(path: String = "/", name: String = "", isRefresh: Boolean = false) = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }
        dirList = myApplication.repository.getDir(url)
        dirInfo.value = dirList
        if (!isRefresh) {
            fileStack.push(Pair(Pair(name, path), dirList))
            stackSize.value = fileStack.size
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
            dirList = fileStack.peek().second
            dirInfo.value = dirList
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
