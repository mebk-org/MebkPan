package com.mebk.pan.utils

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class RetrofitClient(val context: Context) {
    private lateinit var DO_NOT_VERIFY: HostnameVerifier
    private lateinit var sslContext: SSLContext
    private lateinit var retrofit: Retrofit


    /*
    * 忽略ca证书
    * */
    fun ignoreCertificate() {
        // TODO: 2021/4/10 临时

        val xtm = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

            }

            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
            }

            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOfNulls(0)
            }
        }

        sslContext = SSLContext.getInstance("SSL")
        sslContext!!.init(null, arrayOf<TrustManager>(xtm), SecureRandom())
        DO_NOT_VERIFY = HostnameVerifier { hostname, session -> true }
    }

    fun initRetrofit(): Retrofit {
        ignoreCertificate()
        //日志拦截器
        val httpLoggingInterceptor = HttpLoggingInterceptor()
                .setLevel(if (CONFIGURE_DEBUG) (HttpLoggingInterceptor.Level.BODY) else (HttpLoggingInterceptor.Level.NONE))

        val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(AddCookiesInterceptor(context))
                .addInterceptor(httpLoggingInterceptor)
                .connectTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .build()

        retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
        return retrofit
    }


}

