package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.UserDto
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val application = getApplication<MyApplication>()
    lateinit var userInfo: MutableLiveData<UserDto>
    var loginInfo = MutableLiveData<String>().also {
        it.value = "登陆失败"
    }

    fun login(username: String, pwd: String, captchaCode: String) = viewModelScope.launch {
        var response = application.repository.getUser(username, pwd, captchaCode)
        if (response.code() == 200) {
            when (response.body()!!.code) {
                0 -> {
                    loginInfo.value = "登陆成功"
                    userInfo = MutableLiveData<UserDto>().also {
                        it.value = response.body()
                    }
                }
                else -> {
                    loginInfo.value = response.body()!!.msg
                }
            }
        }
    }
}