package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.REQUEST_ERR
import com.mebk.pan.utils.REQUEST_SUCCESS
import com.mebk.pan.utils.REQUEST_TIMEOUT
import kotlinx.coroutines.launch

class ShareHistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication
    private var list = mutableListOf<ShareHistoryEntity>()

    val shareHistoryInfo = MutableLiveData<List<ShareHistoryEntity>>()

    fun getShareHistory(page: Int) = viewModelScope.launch {
        val result = myApplication.repository.shareHistory(MyApplication.uid!!, page)
        when (result.first) {
            REQUEST_SUCCESS -> {
                list = result.second.toMutableList()
                LogUtil.err(this@ShareHistoryViewModel.javaClass, "list=$list")
                shareHistoryInfo.value = list
            }
            REQUEST_TIMEOUT -> {

            }
            REQUEST_ERR -> {
                LogUtil.err(this@ShareHistoryViewModel.javaClass, "err=${result.second}")
            }
        }
    }
}