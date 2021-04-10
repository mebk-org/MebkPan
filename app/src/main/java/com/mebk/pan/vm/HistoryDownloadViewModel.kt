package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.*
import com.mebk.pan.application.MyApplication

class HistoryDownloadViewModel(application: Application) : AndroidViewModel(application) {

    private val myApplication = application as MyApplication
    val downloadInfo = myApplication.repository.getDownloadingInfo().asLiveData()

}