package com.mebk.pan.vm

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.User
import com.mebk.pan.utils.SharePreferenceUtils
import kotlinx.coroutines.launch

class UserInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val myApplication = application as MyApplication

    val userInfoData = MutableLiveData<User>().also {
        viewModelScope.launch {
            if (!TextUtils.isEmpty(MyApplication.uid)) {
                it.value = myApplication.repository.getUserInfo(MyApplication.uid!!)
            }
        }
    }

}