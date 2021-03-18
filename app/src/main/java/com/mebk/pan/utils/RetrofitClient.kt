package com.mebk.pan.utils

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient {
    private lateinit var retrofit: Retrofit
    var cookies = listOf("")


    fun initRetrofit(): Retrofit {

        //日志拦截器
        val httpLoggingInterceptor = HttpLoggingInterceptor()
                .setLevel(if (Configure.CONFIGURE_DEBUG) (HttpLoggingInterceptor.Level.BODY) else (HttpLoggingInterceptor.Level.NONE))

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(AddCookiesInterceptor(cookies))
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(HttpConfigure.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        return retrofit
    }


}

