package com.mebk.pan.vm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.User
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    companion object {
        const val LOGIN_SUCCESS = "登录成功"
    }

    private val application = getApplication<MyApplication>()
    private lateinit var userInfo: MutableLiveData<UserDto>
    var loginInfo = MutableLiveData<String>()


    fun login(username: String, pwd: String, captchaCode: String) = viewModelScope.launch {
        val response = application.repository.getUser(username, pwd, captchaCode)

        when (response.first) {
            RetrofitClient.REQUEST_SUCCESS -> {
                loginInfo.value = LOGIN_SUCCESS
                userInfo = MutableLiveData<UserDto>().also {
                    it.value = response.second
                }
                application.repository.insertUser(
                        with(response.second!!) {
                            User(id, anonymous, avatar, created_at, nickname, preferred_theme, score, status, user_name, group.id, group.name,
                                    policy.allowSource, policy.maxSize, policy.saveType, policy.upUrl)
                        }
                )
            }
            RetrofitClient.REQUEST_TIMEOUT -> {
                loginInfo.value = RetrofitClient.REQUEST_TIMEOUT
            }
            else -> {
                LogUtil.err(this@LoginViewModel::class.java, response.first)
                loginInfo.value = "未知错误，请联系管理员"
            }
        }
    }
}