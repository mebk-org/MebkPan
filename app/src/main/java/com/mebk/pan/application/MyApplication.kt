package com.mebk.pan.application

import android.app.Application
import android.content.Context
import android.text.TextUtils
import com.mebk.pan.repository.Repository
import com.mebk.pan.utils.SharePreferenceUtils
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure

class MyApplication : Application() {
    val repository by lazy { Repository(applicationContext) }

    companion object {
        var isLogin = false
        val cookieList = mutableListOf<String>()
        var path: String? = ""
        var uid: String? = ""
    }

    override fun onCreate() {
        super.onCreate()
        with(SharePreferenceUtils.getSharePreference(this)) {
            isLogin = getBoolean(SharePreferenceUtils.SP_KEY_LOGIN, false)
            uid = getString(SharePreferenceUtils.SP_KEY_UID, "")
            path = getString(SharePreferenceUtils.SP_KEY_DOWNLOAD_DIR_KEY, "")
            if (TextUtils.isEmpty(path)) {
                with(edit()) {
                    putString(SharePreferenceUtils.SP_KEY_DOWNLOAD_DIR_KEY, this@MyApplication.getExternalFilesDir(null)!!.path + "/")
                    commit()
                }
                path = this@MyApplication.getExternalFilesDir(null)!!.path + "/"
            }

        }


        /**
         * 注意: 即使您已经在AndroidManifest.xml中配置过appkey和channel值，也需要在App代码中调
         * 用初始化接口（如需要使用AndroidManifest.xml中配置好的appkey和channel值，
         * UMConfigure.init调用中appkey和channel参数请置为null）。
         */
        UMConfigure.init(this, "606081bcb8c8d45c13b38b53", "XIAOMI", UMConfigure.DEVICE_TYPE_PHONE, null)
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO)
        UMConfigure.setLogEnabled(true)
    }


}
