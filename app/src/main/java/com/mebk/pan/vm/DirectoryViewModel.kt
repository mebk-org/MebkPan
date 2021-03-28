package com.mebk.pan.vm

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.SharePreferenceUtils
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DirectoryViewModel(application: Application) : AndroidViewModel(application) {


    private val application = getApplication<MyApplication>()

    var directoryList = mutableListOf<DirectoryDto.Object>()

    var directoryInfo = MutableLiveData<MutableList<DirectoryDto.Object>>().also {}

    private var requestInfo = MutableLiveData<String>().also {
        it.value = "获取失败"
    }
    var lastRefreshTimeInfo = MutableLiveData<String>().also {
        it.value = getLastRefreshTime()
    }

    fun directory() = viewModelScope.launch {
        LogUtil.err(this::class.java,"获取文件")
        val response = application.repository.getDirectory()
        if (response.code() == 200) {
            requestInfo.value = "获取成功"
            if (response.body()!!.code == 0) {
                directoryList = response.body()!!.data.objects as MutableList
                directoryInfo.value = directoryList

                //获取文件时也要更新刷新时间
                lastRefreshTimeInfo.value = getLastRefreshTime()
            }
        } else {
            requestInfo.value = response.body()!!.msg
        }
    }

    //获取刷新时间
    private fun getLastRefreshTime(): String {
        val sp = SharePreferenceUtils.getSharePreference(application.applicationContext)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
        //获取当前时间
        val date = simpleDateFormat.format(Date(System.currentTimeMillis()))
        with(sp.edit()) {
            putString(com.mebk.pan.utils.SharePreferenceUtils.SP_KEY_REFRESH_TIME, date)
            commit()
        }
        return date
    }

}