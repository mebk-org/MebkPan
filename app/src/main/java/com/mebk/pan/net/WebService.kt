package com.mebk.pan.net

import com.mebk.pan.dtos.*
import com.mebk.pan.utils.*
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

    //删除文件
    @HTTP(method = "DELETE", hasBody = true, path = API_FILE_ACTION)
    suspend fun deleteFile(@Body body: RequestBody): Response<ActionDto>

    //移动文件
    @PATCH(API_FILE_ACTION)
    suspend fun moveFile(@Body body: RequestBody): Response<ActionDto>

    //分享文件
    @POST(API_FILE_SHARE)
    suspend fun shareFile(@Body body: RequestBody): Response<ShareDto>

    //分享记录
    @GET("{path}")
    suspend fun getShareHistory(@Path("path") path: String, @Query("page") page: Int, @Query("type") type: String): Response<ShareHistoryDto>
}