package com.mebk.pan.utils

import android.content.Context
import com.mebk.pan.application.MyApplication
import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(var context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val builder = chain.request().newBuilder()
        if (MyApplication.isLogin) {
            for (cookie in MyApplication.cookieList) {
                builder.addHeader("Cookie", cookie)
            }
        }

        return chain.proceed(builder.build())
    }
}