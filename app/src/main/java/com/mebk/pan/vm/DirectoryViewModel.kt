package com.mebk.pan.vm

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
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

    fun directory(isRefresh: Boolean = false) = viewModelScope.launch {
        if (!TextUtils.isEmpty(getLastRefreshTime()) && !isRefresh) {
            //从本地数据库读取
            directoryList = application.repository.getFile()
            directoryInfo.value = directoryList
        } else {
            //从网络获取
            getNetFile()
        }
    }

    fun directory(name: String, path: String = "/") = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }
        val pair = application.repository.getInternalFile(url)
        if (pair.first == RetrofitClient.REQUEST_SUCCESS) {
            requestInfo.value = "获取成功"
            LogUtil.err(this.javaClass, pair.second.toString())
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
            directoryList = pair.second!!.objects.map {
                File(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
            }
//            fileInfo.value = fileList
            directoryInfo.value = directoryList
        } else {
            requestInfo.value = pair.first
        }
    }


    //获取刷新时间
    private fun getLastRefreshTime(): String {
        val sp = SharePreferenceUtils.getSharePreference(application.applicationContext)
        return sp.getString(SharePreferenceUtils.SP_KEY_REFRESH_TIME, "") ?: return ""
    }


    //更新刷新时间
    private fun setLastRefreshTime(): String {
        val sp = SharePreferenceUtils.getSharePreference(application.applicationContext)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        //获取当前时间
        val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
        with(sp.edit()) {
            putString(SharePreferenceUtils.SP_KEY_REFRESH_TIME, date)
            commit()
        }
        return date
    }

    //获取网络文件
    private fun getNetFile() = viewModelScope.launch {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        val pair = application.repository.getDirectory()
        when (pair.first) {
            RetrofitClient.REQUEST_SUCCESS -> {
                requestInfo.value = RetrofitClient.REQUEST_SUCCESS
                directoryList = pair.second!!.objects.map {
                    File(it.id, it.name, it.path, it.pic, it.size, it.type, format.parse(it.date)!!.time, "")
                }
                directoryInfo.value = directoryList

                //获取文件时也要更新刷新时间
                lastRefreshTimeInfo.value = setLastRefreshTime()

            }
            RetrofitClient.REQUEST_TIMEOUT -> {
                requestInfo.value = "链接超时，请重试"
            }
            else -> {
                LogUtil.err(this@DirectoryViewModel.javaClass, pair.first)
                requestInfo.value = pair.first
            }
        }
    }


}