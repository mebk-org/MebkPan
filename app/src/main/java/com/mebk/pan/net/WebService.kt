package com.mebk.pan.net

import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.DownloadClientDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.HttpConfigure
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


interface WebService {
    //登录接口
    @Headers(HttpConfigure.CONTENT_TYPE_JSON)
    @POST(HttpConfigure.API_LOGIN)
    suspend fun loginApi(@Body body: RequestBody): Response<UserDto>

    //获取网盘文件
    @GET(HttpConfigure.API_DIRECTORY)
    suspend fun getDirectoryApi(): Response<DirectoryDto>

    //获取文件夹下内容
    @GET("{path}")
    suspend fun getInternalFile(@Path("path") path: String): Response<DirectoryDto>

    //获取下载链接
    @PUT("{path}")
    suspend fun getDownloadFileClient(@Path("path") path: String): Response<DownloadClientDto>
}