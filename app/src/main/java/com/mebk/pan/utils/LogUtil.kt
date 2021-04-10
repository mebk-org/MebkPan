package com.mebk.pan.utils

import android.util.Log

class LogUtil {
    companion object {

        fun <T> err(clazz: Class<T>, err: String) {
            if (CONFIGURE_DEBUG) {
                Log.e(clazz.name, "err:=$err ")
            }
        }

    }
}