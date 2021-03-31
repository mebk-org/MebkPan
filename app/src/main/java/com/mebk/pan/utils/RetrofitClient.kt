package com.mebk.pan.utils

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitClient(val context: Context) {
    private lateinit var retrofit: Retrofit

    companion object {
        const val REQUEST_SUCCESS = "SUCCESS"
    }

    fun initRetrofit(): Retrofit {

        //日志拦截器
        val httpLoggingInterceptor = HttpLoggingInterceptor()
                .setLevel(if (Configure.CONFIGURE_DEBUG) (HttpLoggingInterceptor.Level.BODY) else (HttpLoggingInterceptor.Level.NONE))

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AddCookiesInterceptor(context))
                .addInterceptor(httpLoggingInterceptor)
                .build()



        retrofit = Retrofit.Builder()
                .baseUrl(HttpConfigure.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        return retrofit
    }


}

