package com.mebk.pan.vm

import android.app.Application
import android.text.TextUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.LogUtil
import kotlinx.coroutines.launch

class StartViewModel(application: Application) : AndroidViewModel(application) {


    fun getCookie(uid: String) =viewModelScope.launch{
        val user = getApplication<MyApplication>().repository.getUserCookie(uid)
        LogUtil.err(this::class.java,user.toString())
        with(MyApplication.cookieList){
            add(user[0].cookie1!!)
            if (!TextUtils.isEmpty(user[0].cookie2)){
                add(user[0].cookie2!!)
            }
        }


    }


}