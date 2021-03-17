package com.mebk.pan.net

import com.google.gson.JsonObject
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.HttpConfigure
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit

class Repository() {
    private var retrofitClient = RetrofitClient()
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

        if (response.code() == 200 && response.code() == 0) {
            val cookieMap = response.headers().toMultimap()
            retrofitClient.cookies = cookieMap["set-cookie"]!!
        }

        return response
    }
}