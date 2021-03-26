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

        //上次刷新时间
        const val SP_KEY_REFRESH_TIME = "refreshTime"


        fun getSharePreference(context: Context, name: String = SP_USER): SharedPreferences {
            return context.getSharedPreferences(name, Context.MODE_PRIVATE)
        }


    }


}