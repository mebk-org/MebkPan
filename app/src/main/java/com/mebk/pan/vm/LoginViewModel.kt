package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.UserDto
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val LOGIN_SUCCESS = 0
        const val LOGIN_FAILED = 40001
    }

    private val application = getApplication<MyApplication>()
    private lateinit var userInfo: MutableLiveData<UserDto>
    var loginInfo = MutableLiveData<Map<String, String>>().also {
    }


    fun login(username: String, pwd: String, captchaCode: String) = viewModelScope.launch {
        val response = application.repository.getUser(username, pwd, captchaCode)
        if (response.code() == 200) {
            when (response.body()!!.code) {
                LOGIN_SUCCESS -> {
                    loginInfo.value = mapOf("code" to "0", "msg" to "登录成功")
                    userInfo = MutableLiveData<UserDto>().also {
                        it.value = response.body()
                    }
                }
                else -> {
                    loginInfo.value = mapOf("code" to response.body()!!.code.toString(), "msg" to response.body()!!.msg)
                }
            }
        }
    }
}