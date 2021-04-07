package com.mebk.pan.repository

import android.content.Context
import android.os.SystemClock
import com.google.gson.JsonObject
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.DataBase
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.FileUpdateDownloadClient
import com.mebk.pan.database.entity.User
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.net.WebService
import com.mebk.pan.utils.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import java.net.SocketTimeoutException
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

    //存储下载列表
    suspend fun addDownloadInfo(file: DownloadInfo) {
        database.downloadInfoDao().insertDownloadFile(file)
    }

    //更新下载列表
    suspend fun updateDownloadInfo(file: DownloadInfo) {
        database.downloadInfoDao().updateDownloadFile(file)
    }


    //获取用户信息
    suspend fun getUser(username: String, pwd: String, captchaCode: String): Pair<String, UserDto?> {
        val jsonObj = JsonObject()
        jsonObj.addProperty("username", username)
        jsonObj.addProperty("Password", pwd)
        jsonObj.addProperty("captchaCode", captchaCode)
        val requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonObj.toString())
        var pair: Pair<String, UserDto?>
        try {
            val response = retrofit.create(WebService::class.java)
                    .loginApi(requestBody)
            with(response) {

                if (body()!!.code == 0) {
                    MyApplication.isLogin = true
                    with(MyApplication.cookieList) {
                        for (cookie in response.headers().toMultimap()["set-cookie"]!!) {
                            add(cookie)
                        }
                    }
                    var valid: Long = 0
                    for (maxAge in response.headers().toMultimap()["nel"]!!) {
                        LogUtil.err(this@Repository.javaClass, "max_age=${maxAge}")
                        if (maxAge.contains("max_age")) {

                            //查找max_age位置
                            val rangeStartPos = maxAge.indexOf("max_age")
                            val rangeEndPos = maxAge.indexOf(",", rangeStartPos)

                            val endPos = if (rangeEndPos == -1) maxAge.length - 1 else rangeEndPos
                            val startPos = maxAge.indexOf(":", rangeStartPos)
                            if (startPos != -1) {
                                valid = maxAge.substring(startPos + 1, endPos).toLong()
                            }
                        }
                        LogUtil.err(this@Repository.javaClass, "valid=${valid}")
                    }


                    body()?.data?.let {

                        database.userDao().clear()
                        database.userDao().insertUser(User(it.id,
                                it.nickname,
                                this.headers().toMultimap()["set-cookie"]?.get(0),
                                if (this.headers().toMultimap()["set-cookie"]?.size!! > 1) this.headers().toMultimap()["set-cookie"]?.get(1) else ""
                        ))
                        val sharedPref = SharePreferenceUtils.getSharePreference(context)
                        with(sharedPref.edit()) {
                            putBoolean(SharePreferenceUtils.SP_KEY_LOGIN, true)
                            putString(SharePreferenceUtils.SP_KEY_UID, it.id)
                            putLong(SharePreferenceUtils.SP_KEY_LOGIN_TIME, SystemClock.uptimeMillis())
                            putLong(SharePreferenceUtils.SP_KEY_COOKIE_VALID, valid)
                            commit()
                        }
                    }
                    pair = Pair(RetrofitClient.REQUEST_SUCCESS, body()?.data)
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(RetrofitClient.REQUEST_TIMEOUT, null)
        } catch (e: Exception) {
            pair = Pair(e.toString(), null)
        }
        return pair

    }


    //获取网盘文件
    suspend fun getDirectory(): Pair<String, DirectoryDto?> {
        var pair: Pair<String, DirectoryDto?>
        try {
            val response = retrofit.create(WebService::class.java)
                    .getDirectoryApi()

            LogUtil.err(this::class.java, response.body().toString())
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

                        database.fileDao().clear()
                        for (file in it.objects) {
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
                    pair = Pair(RetrofitClient.REQUEST_SUCCESS, body()?.data)
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(RetrofitClient.REQUEST_TIMEOUT, null)
        } catch (e: Exception) {
            pair = Pair(e.toString(), null)
        }
        return pair
    }


    //获取文件夹下内容
    suspend fun getInternalFile(path: String): Pair<String, DirectoryDto?> {
        var pair = Pair<String, DirectoryDto?>("", null)
        try {
            val response = retrofit.create(WebService::class.java)
                    .getInternalFile(ToolUtils.splitUrl(API_DIRECTORY, path))
            LogUtil.err(this::class.java, response.toString())
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        pair = Pair(RetrofitClient.REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(RetrofitClient.REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            pair = Pair(e.toString(), null)
        }


        return pair
    }

    //获取下载链接
    suspend fun getDownloadClient(id: String): Pair<String, String> {
        var pair = Pair<String, String>("", "")
        try {
            val response = retrofit.create(WebService::class.java)
                    .getDownloadFileClient(ToolUtils.splitUrl(API_DOWNLOAD_CLIENT, id))
            LogUtil.err(this::class.java, response.toString())
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        updateDownloadClient(FileUpdateDownloadClient(id, it))
                        pair = Pair(RetrofitClient.REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, "")
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(RetrofitClient.REQUEST_TIMEOUT, "")
        } catch (e: java.lang.Exception) {
            pair = Pair(e.toString(), "")
        }

        return pair
    }


    //下载文件
    suspend fun downloadFile(url: String): ResponseBody {
        return retrofit.create(WebService::class.java)
                .downloadFile(url)
    }


    //获取文件信息
    suspend fun getFileInfo(id: String, isFolder: Boolean, traceRoot: Boolean = false): Pair<String, FileInfoDto?> {
        var pair = Pair<String, FileInfoDto?>("", null)
        try {
            val response = retrofit.create(WebService::class.java)
                    .getFileInfo(ToolUtils.splitUrl(API_FILE_INFO, id), traceRoot, isFolder)
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        pair = Pair(RetrofitClient.REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(RetrofitClient.REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            pair = Pair(e.toString(), null)
        }
        return pair
    }
}