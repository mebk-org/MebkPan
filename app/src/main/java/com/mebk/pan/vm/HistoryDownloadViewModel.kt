package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadingInfoEntity
import kotlinx.coroutines.launch

class HistoryDownloadViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    val downloadListInfo = MutableLiveData<List<DownloadingInfoEntity>>().also {
        viewModelScope.launch {
            it.value = myApplication.repository.getHistoryDownloadList()
        }
    }
}
