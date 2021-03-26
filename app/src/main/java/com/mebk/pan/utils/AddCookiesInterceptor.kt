package com.mebk.pan.utils

import android.content.Context
import com.mebk.pan.application.MyApplication
import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(var context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var builder = chain.request().newBuilder()
        if (MyApplication.isLogin) {
            LogUtil.err(this.javaClass, "添加cookie list.size=${MyApplication.cookieList.size}")
            for (cookie in MyApplication.cookieList) {
                LogUtil.err(this.javaClass, "cookie=$cookie")
                builder.addHeader("Cookie", cookie)
            }
        }

        return chain.proceed(builder.build())
    }
}