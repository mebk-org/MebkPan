package com.mebk.pan.net

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.JsonObject
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.HttpConfigure
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Response

class Repository(val context: Context) {
    private var retrofitClient = RetrofitClient(context)
    private var retrofit = retrofitClient.initRetrofit()


    suspend fun getUser(username: String, pwd: String, captchaCode: String): Response<UserDto> {
        val jsonObj = JsonObject()
        jsonObj.addProperty("username", username)
        jsonObj.addProperty("Password", pwd)
        jsonObj.addProperty("captchaCode", captchaCode)
        val requestBody = RequestBody.create(MediaType.parse(HttpConfigure.CONTENT_TYPE_JSON), jsonObj.toString())
        val response = retrofit.create(WebService::class.java)
                .loginApi(requestBody)
        LogUtil.err(this::class.java, response.body().toString())

        if (response.code() == 200 && response.body()!!.code == 0) {

            val sharedPref = SharePreferenceUtils.getSharePreference(context)
            var set = hashSetOf("")
            with(sharedPref.edit()) {
                set.clear()
                for (cookie in response.headers().toMultimap().get("set-cookie")!!) {
                    LogUtil.err(this::class.java, cookie)
                    set.add(cookie)
                }
                putStringSet(SharePreferenceUtils.SP_KEY_COOKIE, set)
                commit()
            }
        }

        return response
    }

    //获取网盘文件
    suspend fun getDirectory(): Response<DirectoryDto> {
        val response = retrofit.create(WebService::class.java)
                .getDirectoryApi()
        LogUtil.err(this::class.java, response.body().toString())

        return response
    }

}