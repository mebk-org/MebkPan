package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.mebk.pan.application.MyApplication

class DownloadingViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    val downloadingListInfo = myApplication.repository.getDownloadingList().asLiveData()
}