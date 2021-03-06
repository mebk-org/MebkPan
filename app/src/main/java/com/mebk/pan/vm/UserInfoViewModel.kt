package com.mebk.pan.vm

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.database.entity.UserEntity
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.REQUEST_ERR
import com.mebk.pan.utils.REQUEST_SUCCESS
import com.mebk.pan.utils.REQUEST_TIMEOUT
import kotlinx.coroutines.launch

class UserInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication


    val userInfoData = MutableLiveData<UserEntity>().also {
        viewModelScope.launch {
            if (!TextUtils.isEmpty(MyApplication.uid)) {
                it.value = myApplication.repository.getUserInfo(MyApplication.uid!!)
                LogUtil.err(this@UserInfoViewModel.javaClass, "userinfo=$it")
            }
        }
    }



}