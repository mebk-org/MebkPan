package com.mebk.pan.vm

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {


    private val application = getApplication<MyApplication>()

    private var directoryList = listOf<File>()

    var directoryInfo = MutableLiveData<List<File>>()

    var requestInfo = MutableLiveData<String>()
    var lastRefreshTimeInfo = MutableLiveData<String>().also {
        it.value = getLastRefreshTime()
    }
    private val fileStack = Stack<Pair<String, List<File>>>()

    var stackSize = MutableLiveData<Int>().also {
        it.value = fileStack.size
    }

    /**
     * 获取文件列表
     * @param isRefresh Boolean
     * @return Job
     */
    fun directory(isRefresh: Boolean = false) = viewModelScope.launch {
        if (!TextUtils.isEmpty(getLastRefreshTime()) && !isRefresh) {
            //从本地数据库读取
            directoryList = application.repository.getFile()
            directoryInfo.value = directoryList
        } else {
            //从网络获取
            getNetFile()
        }
        if (stackSize.value == 0) {
            fileStack.push(Pair("/", directoryList))
            stackSize.value = fileStack.size
        }
    }

    /**
     * 获取文件夹下文件列表
     * @param isRefresh Boolean 刷新状态码
     * @param name String 文件夹名
     * @param path String 文件夹路径
     * @return Job
     */
    fun directory(name: String, path: String = "/", isRefresh: Boolean = false) = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }

        if (!TextUtils.isEmpty(getLastRefreshTime(name)) && !isRefresh) {
            //从本地数据库读取
            LogUtil.err(this@DirectoryViewModel.javaClass, "path=${url}")
            directoryList = application.repository.getFile(url)
            directoryInfo.value = directoryList

        } else {
            //从网络获取
            getNetFile(name, url)
        }
        fileStack.push(Pair(name, directoryList))
        stackSize.value = fileStack.size
    }

    /**
     * 获取网络文件夹下文件
     * @param name String 文件夹名
     * @param path String 文件夹路径
     * @return Job
     */
    private fun getNetFile(name: String, url: String) = viewModelScope.launch {
        val pair = application.repository.getInternalFile(url)
        if (pair.first == REQUEST_SUCCESS) {
            requestInfo.value = "获取成功"
            LogUtil.err(this.javaClass, pair.second.toString())
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            directoryList = pair.second!!.objects.map {
                File(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
            }
            directoryList.forEach {
                application.repository.addFile(it)
            }
            directoryInfo.value = directoryList

            //获取文件时也要更新刷新时间
            lastRefreshTimeInfo.value = setLastRefreshTime(name)
        } else {
            requestInfo.value = pair.first
        }
    }


    //获取刷新时间
    private fun getLastRefreshTime(name: String = "/"): String {

        val sp = SharePreferenceUtils.getSharePreference(application.applicationContext)
        val set = sp.getStringSet(SharePreferenceUtils.SP_KEY_REFRESH_TIME, setOf())
        set?.let {
            if (name in set) {
                return sp.getString(name, "") ?: ""
            }
        }
        return ""
    }


    //更新刷新时间
    private fun setLastRefreshTime(name: String = "/"): String {
        val sp = SharePreferenceUtils.getSharePreference(application.applicationContext)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        //获取当前时间
        val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
        val set = sp.getStringSet(SharePreferenceUtils.SP_KEY_REFRESH_TIME, setOf())
        val newSet = set?.union(setOf(name))
        with(sp.edit()) {
            putStringSet(SharePreferenceUtils.SP_KEY_REFRESH_TIME, newSet)
            putString(name, date)
            commit()
        }
        return date
    }

    /**
     * 获取网络文件
     * @return Job
     */
    private fun getNetFile() = viewModelScope.launch {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val pair = application.repository.getDirectory()
        when (pair.first) {
            REQUEST_SUCCESS -> {
                requestInfo.value = REQUEST_SUCCESS
                directoryList = pair.second!!.objects.map {
                    File(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
                }
                directoryInfo.value = directoryList

                //获取文件时也要更新刷新时间
                lastRefreshTimeInfo.value = setLastRefreshTime()

            }
            REQUEST_TIMEOUT -> {
                requestInfo.value = "链接超时，请重试"
            }
            else -> {
                LogUtil.err(this@DirectoryViewModel.javaClass, pair.first)
                requestInfo.value = pair.first
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
            directoryList = fileStack.peek().second
            directoryInfo.value = directoryList
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