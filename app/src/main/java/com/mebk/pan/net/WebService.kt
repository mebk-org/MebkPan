package com.mebk.pan.net

import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.HttpConfigure
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.Headers
import retrofit2.http.POST

interface WebService {
    //登录接口
    @Headers(HttpConfigure.CONTENT_TYPE_JSON)
    @POST(HttpConfigure.API_LOGIN)
    suspend fun loginApi(@Body body: RequestBody): Response<UserDto>


}