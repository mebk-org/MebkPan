package com.mebk.pan.repository

import android.content.Context
import android.text.TextUtils
import com.google.gson.JsonObject
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.DataBase
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.User
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.net.WebService
import com.mebk.pan.utils.HttpConfigure
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.SharePreferenceUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Response
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
        LogUtil.err(this.javaClass,"从本地读取")
        return database.fileDao().getFile()
    }

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
                add(response.headers().toMultimap()["set-cookie"]?.get(0)!!)
                add(response.headers().toMultimap()["set-cookie"]?.get(1)!!)
            }

            database.userDao().insertUser(User(response.body()!!.data.id,
                    response.body()!!.data.nickname,
                    response.headers().toMultimap()["set-cookie"]?.get(0),
                    response.headers().toMultimap()["set-cookie"]?.get(1)
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

            for (file in response.body()!!.data.objects) {
                with(database.fileDao()) { insertFile(File(
                            file.id,
                            file.name,
                            file.path,
                            file.pic,
                            file.size,
                            file.type,
                            format.parse(file.date).time))
                }
            }
        }


        return response
    }

    //获取文件夹下内容
    suspend fun getInternalFile(path: String): Response<DirectoryDto> {
        val response = retrofit.create(WebService::class.java)
                .getInternalFile(HttpConfigure.internalFile(path))
        LogUtil.err(this::class.java, response.body().toString())

        return response
    }
}