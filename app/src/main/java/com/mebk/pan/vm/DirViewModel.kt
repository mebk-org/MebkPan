package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch

class DirViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    private var dirList = mutableListOf<File>()
    var dirInfo = MutableLiveData<List<File>>()

    fun getDir(path: String = "/", name: String = "") = viewModelScope.launch {
        val url = if (path != "/") {
            "$path/$name"
        } else {
            path + name
        }
        LogUtil.err(this@DirViewModel.javaClass,"utl=$url")
        dirList.clear()
        dirList.addAll(myApplication.repository.getDir(url))
        dirInfo.value = dirList
    }


}