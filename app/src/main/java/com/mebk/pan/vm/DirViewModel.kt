package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.FileEntity
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.REQUEST_SUCCESS
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DirViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    private var dirList = listOf<FileEntity>()
    var dirInfo = MutableLiveData<List<FileEntity>>()
    private val fileStack = Stack<Pair<Pair<String, String>, List<FileEntity>>>()
    var stackSize = MutableLiveData<Int>().also {
        it.value = fileStack.size
    }

    val resultInfo = MutableLiveData<String>()

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
                dirList = pair.second!!.objects.map {
                    FileEntity(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
                }
                dirList.forEach {
                    myApplication.repository.addFile(it)
                }
                dirList = dirList.filter { it.type == "dir" }
                dirInfo.value = dirList
            }
        }
    }

    /**
     * 移动文件
     * @param srcDir String 文件当前路径
     * @param dirList List<String> 需要移动的文件列表
     * @param fileList List<String> 需要移动的文件夹列表
     * @param dst String  移动的目标路径
     * @return Job
     */
    fun move(srcDir: String, dirList: List<String>, fileList: List<String>, dst: String) = viewModelScope.launch {
        val pair = myApplication.repository.moveFile(srcDir, dirList, fileList, dst)
        when (pair.first) {
            REQUEST_SUCCESS -> {
                val list = dirList + fileList
                myApplication.repository.changePath(list, dst)
            }
        }
        resultInfo.value=pair.first
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

    /**
     * 获取当前文件的路径
     * @return String
     */
    fun getUrl(): String {
        return if (getStackFirst().first != "/") {
            "${getStackFirst().first}/${getStackFirst().second}"
        } else {
            getStackFirst().first + getStackFirst().second
        }
    }
}
