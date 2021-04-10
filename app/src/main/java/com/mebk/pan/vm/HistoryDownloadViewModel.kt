package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HistoryDownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val myApplication= application as MyApplication
    val downloadInfo = myApplication.repository.getHistoryDownload().asLiveData()

}