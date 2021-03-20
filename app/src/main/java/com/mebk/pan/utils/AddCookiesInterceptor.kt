package com.mebk.pan.utils

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(var context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        LogUtil.err(this::class.java, "添加cookie")
        var builder = chain.request().newBuilder()
        val sharedPreferences = context.getSharedPreferences("userSP", Context.MODE_PRIVATE)
        val set = sharedPreferences.getStringSet("cookie", mutableSetOf())

        set?.let {
            if (set.size != 0) {
                for (cookie in set) {
                    builder.addHeader("Cookie", cookie)
                }
            }
        }
        return chain.proceed(builder.build())
    }
}