package com.mebk.pan.net

import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.DownloadClientDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.utils.API_DIRECTORY
import com.mebk.pan.utils.API_LOGIN
import com.mebk.pan.utils.CONTENT_TYPE_JSON
import com.mebk.pan.utils.ResponseData
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface WebService {
    //登录接口
    @Headers(CONTENT_TYPE_JSON)
    @POST(API_LOGIN)
    suspend fun loginApi(@Body body: RequestBody): Response<ResponseData<UserDto>>

    //获取网盘文件
    @GET(API_DIRECTORY)
    suspend fun getDirectoryApi(): Response<ResponseData<DirectoryDto>>

    //获取文件夹下内容
    @GET("{path}")
    suspend fun getInternalFile(@Path("path") path: String): Response<ResponseData<DirectoryDto>>

    //获取下载链接
    @PUT("{path}")
    suspend fun getDownloadFileClient(@Path("path") path: String): Response<DownloadClientDto>

    //下载文件
    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody

    //获取文件详细信息
    @GET("{path}")
    suspend fun getFileInfo(@Path("path") path: String,
                            @Query("trace_root") traceRoot: Boolean,
                            @Query("is_folder") isFolder: Boolean): Response<ResponseData<FileInfoDto>>
}