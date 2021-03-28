package com.mebk.pan.application

import android.app.Application
import com.mebk.pan.repository.Repository
import com.mebk.pan.utils.SharePreferenceUtils

class MyApplication : Application() {
    val repository by lazy { Repository(applicationContext) }

    companion object {
        var isLogin = false
        val cookieList = mutableListOf<String>()
    }

    override fun onCreate() {
        super.onCreate()
        isLogin = SharePreferenceUtils.getSharePreference(this).getBoolean(SharePreferenceUtils.SP_KEY_LOGIN, false)
    }

}
