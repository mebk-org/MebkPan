package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.REQUEST_SUCCESS
import com.mebk.pan.utils.REQUEST_TIMEOUT
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DirViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    private var dirList = listOf<File>()
    var dirInfo = MutableLiveData<List<File>>()
    private val fileStack = Stack<Pair<Pair<String, String>, List<File>>>()
    var stackSize = MutableLiveData<Int>().also {
        it.value = fileStack.size
    }

    fun getDir(path: String = "/", name: String = "") = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }
        LogUtil.err(this@DirViewModel.javaClass, "url=$url")
        dirList = myApplication.repository.getDir(url)
        dirInfo.value = dirList
        fileStack.push(Pair(Pair(path, name), dirList))
        stackSize.value = fileStack.size

    }

    fun getDirNetWork(path: String = "/", name: String = "") = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }
        val pair = myApplication.repository.getInternalFile(url)
        when (pair.first) {
            REQUEST_SUCCESS -> {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                dirList = pair.second!!.objects.filter { it.type == "dir" }.map {
                    File(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
                }
                dirInfo.value = dirList
            }
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

    /**
     * 查看当前的文件夹名与路径
     * @return Pair<String, String>
     */
    fun getStackFirst() = fileStack.peek().first
}
