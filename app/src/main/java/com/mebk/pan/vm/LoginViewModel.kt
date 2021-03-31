package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val LOGIN_SUCCESS = 0
    }

    private val application = getApplication<MyApplication>()
    private lateinit var userInfo: MutableLiveData<UserDto>
    var loginInfo = MutableLiveData<Map<String, String>>()


    fun login(username: String, pwd: String, captchaCode: String) = viewModelScope.launch {
        val response = application.repository.getUser(username, pwd, captchaCode)
        if (response.first == RetrofitClient.REQUEST_SUCCESS) {
            loginInfo.value = mapOf("code" to "0", "msg" to "登录成功")
            userInfo = MutableLiveData<UserDto>().also {
                it.value = response.second
            }
        } else {
//            loginInfo.value = mapOf("code" to response.second!!.code.toString(), "msg" to response.first)
        }
    }
}