package com.mebk.pan.utils

import okhttp3.Interceptor
import okhttp3.Response

class AddCookiesInterceptor(var cookies: List<String>?) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var builder = chain.request().newBuilder()
        for (cookie in cookies!!) {
            builder.addHeader("Cookie", cookie)
        }
        return chain.proceed(builder.build());
    }
}