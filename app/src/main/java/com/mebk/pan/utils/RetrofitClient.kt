package com.mebk.pan.utils

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitClient(val context: Context) {
    private lateinit var retrofit: Retrofit

    companion object {
        const val REQUEST_SUCCESS = "SUCCESS"
        const val REQUEST_TIMEOUT = "TIMEOUT"
        const val DOWNLOAD_STATE_WAIT = 0
        const val DOWNLOAD_STATE_PREPARE = 1
        const val DOWNLOAD_STATE_DONE = 2
        const val DOWNLOAD_STATE_DOWNLOADING = 3
        const val DOWNLOAD_STATE_ERR = 4
        const val DOWNLOAD_STATE_NETWORK_ERR = 5


        fun checkDownloadState(state: Int): String {
            return when (state) {
                DOWNLOAD_STATE_WAIT -> "等待中"
                DOWNLOAD_STATE_PREPARE -> "获取链接"
                DOWNLOAD_STATE_DONE -> "下载完成"
                DOWNLOAD_STATE_DOWNLOADING -> "下载中"
                DOWNLOAD_STATE_ERR -> "错误"
                DOWNLOAD_STATE_NETWORK_ERR -> "网络错误"
                else -> "状态码不正确，code=${state}"
            }
        }

    }


    fun initRetrofit(): Retrofit {

        //日志拦截器
        val httpLoggingInterceptor = HttpLoggingInterceptor()
                .setLevel(if(CONFIGURE_DEBUG) (HttpLoggingInterceptor.Level.BODY) else (HttpLoggingInterceptor.Level.NONE))

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AddCookiesInterceptor(context))
//                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .build()



        retrofit = Retrofit.Builder()
                .baseUrl(HttpConfigure.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        return retrofit
    }


}

