package com.mebk.pan.utils

import android.content.Context
import android.content.SharedPreferences

class SharePreferenceUtils {

    companion object {
        /**
         * 文件名
         * */
        const val SP_USER = "userSP"

        /*
        * key
        * */
        const val SP_KEY_COOKIE = "cookies"

        const val SP_KEY_LOGIN = "login"

        const val SP_KEY_UID = "uid"

        //上次登陆时间
        const val SP_KEY_LOGIN_TIME = "login_time"

        //cookie有效期
        const val SP_KEY_COOKIE_VALID = "cookie_valid"


        //上次刷新时间
        const val SP_KEY_REFRESH_TIME = "refreshTime"

        //下载位置
        const val SP_KEY_DOWNLOAD_DIR_KEY = "download_dir"

        fun getSharePreference(context: Context, name: String = SP_USER): SharedPreferences {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }


    }


}