package com.mebk.pan.repository

import android.content.Context
import com.google.gson.JsonObject
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.DataBase
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.FileUpdateDownloadClient
import com.mebk.pan.database.entity.User
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.DownloadClientDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.net.WebService
import com.mebk.pan.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import java.text.SimpleDateFormat
import java.util.*

class Repository(val context: Context) {
    private var retrofitClient = RetrofitClient(context)
    private var retrofit = retrofitClient.initRetrofit()

    private var database = DataBase.getDatabase(context)


    suspend fun getUserCookie(uid: String): List<User> {
        return database.userDao().getUserCookie(uid)
    }

    suspend fun getFile(): List<File> {
        LogUtil.err(this.javaClass, "从本地读取")
        return database.fileDao().getFile()
    }

    suspend fun updateDownloadClient(file: FileUpdateDownloadClient) {
        database.fileDao().updateDownloadClient(file)
    }

    //获取用户信息
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

            MyApplication.isLogin = true
            with(MyApplication.cookieList) {
                for (cookie in response.headers().toMultimap()["set-cookie"]!!) {
                    add(cookie)
                }
            }

            database.userDao().clear()
            database.userDao().insertUser(User(response.body()!!.data.id,
                    response.body()!!.data.nickname,
                    response.headers().toMultimap()["set-cookie"]?.get(0),
                    if (response.headers().toMultimap()["set-cookie"]?.size!! > 1) response.headers().toMultimap()["set-cookie"]?.get(1) else ""
            ))

            val sharedPref = SharePreferenceUtils.getSharePreference(context)
            with(sharedPref.edit()) {
                putBoolean(SharePreferenceUtils.SP_KEY_LOGIN, true)
                putString(SharePreferenceUtils.SP_KEY_UID, response.body()!!.data.id)
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

        if (response.code() == 200 && response.body()!!.code == 0) {
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

            database.fileDao().clear()
            for (file in response.body()!!.data.objects) {
                with(database.fileDao()) {
                    insertFile(File(
                            file.id,
                            file.name,
                            file.path,
                            file.pic,
                            file.size,
                            file.type,
                            format.parse(file.date).time,
                            ""))
                }
            }
        }


        return response
    }

    //获取文件夹下内容
    suspend fun getInternalFile(path: String): Response<DirectoryDto> {
        val response = retrofit.create(WebService::class.java)
                .getInternalFile(ToolUtils.splitUrl(HttpConfigure.API_DIRECTORY, path))
        LogUtil.err(this::class.java, response.body().toString())

        return response
    }

    //获取下载链接
    suspend fun getDownloadClient(id: String): Response<DownloadClientDto> {
        val response = retrofit.create(WebService::class.java)
                .getDownloadFileClient(ToolUtils.splitUrl(HttpConfigure.API_DOWNLOAD_CLIENT, id))
        LogUtil.err(this::class.java, response.body().toString())

        if (response.code() == 200 && response.body()!!.code == 0) {
            updateDownloadClient(FileUpdateDownloadClient(id, response.body()!!.data))
        }

        return response
    }


    //下载文件
    suspend fun downloadFile(url: String): ResponseBody {
        return retrofit.create(WebService::class.java)
                .downloadFile(url)
    }


    //获取文件信息
    suspend fun getFileInfo(id: String, isFolder: Boolean, traceRoot: Boolean = false): Response<FileInfoDto> {
        val response = retrofit.create(WebService::class.java)
                .getFileInfo(ToolUtils.splitUrl(HttpConfigure.API_FILE_INFO, id), traceRoot, isFolder)
        LogUtil.err(this::class.java, response.body().toString())

        return response
    }
}