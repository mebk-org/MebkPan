package com.mebk.pan.repository

import android.content.Context
import android.os.SystemClock
import com.google.gson.JsonObject
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.DataBase
import com.mebk.pan.database.entity.*
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.dtos.FileInfoDto
import com.mebk.pan.dtos.UserDto
import com.mebk.pan.net.WebService
import com.mebk.pan.utils.*
import kotlinx.coroutines.flow.Flow
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

    /**
     *从本地获取cookie
     */
    suspend fun getUserCookie(uid: String): List<UserCookie> = database.cookieDao().getUserCookie(uid)


    /**
     * 从本地获取文件
     */
    suspend fun getFile(): List<File> = database.fileDao().getFile()


    /**
     * 更新本地下载链接
     */
    suspend fun updateDownloadClient(file: FileUpdateDownloadClient) = database.fileDao().updateDownloadClient(file)

    /**
     * 获取下载列表
     */
    fun getDownloadingInfo(): Flow<List<DownloadingInfo>> = database.downloadingInfoDao().getDownloadInfo()

    /**
     * 存储下载列表
     */
    suspend fun addDownloadingInfo(file: DownloadingInfo) = database.downloadingInfoDao().insertDownloadFile(file)

    /**
     * 删除下载列表
     */
    suspend fun deleteDownloadingInfo(file: DownloadingInfo) = database.downloadingInfoDao().delete(file)

    /**
     * 更新下载状态
     */
    suspend fun updateDownloadingState(fileId: String, state: Int) = database.downloadingInfoDao().updateDownloadFileState(fileId, state)

    /**
     * 更新下载链接
     */
    suspend fun updateDownloadingClient(fileId: String, client: String) = database.downloadingInfoDao().updateDownloadFileClient(fileId, client)

    /**
     * 更新工作ID
     */
    suspend fun updateDownloadingWorkId(fileId: String, workerId: String) = database.downloadingInfoDao().updateDownloadFileWorkerId(fileId, workerId)

    /**
     * 更新完成时间
     */
    suspend fun updateDownloadingDate(fileId: String, date: Long) = database.downloadingInfoDao().updateDownloadDate(fileId, date)
    /**
     * 更新文件路径
     */
    suspend fun updateDownloadFilePath(fileId: String, path: String) = database.downloadingInfoDao().updateDownloadPath(fileId, path)

    /**
     * 获取正在下载的列表
     * @return List<DownloadingInfo>
     */
    fun getDownloadingList() = database.downloadingInfoDao().getDownloadingList()

    /**
     * 获取下载完成列表
     * @return List<DownloadingInfo>
     */
    fun getDownloadDoneList() = database.downloadingInfoDao().getDownloadDoneList()

    /**
     * 查询库中处于prepare的数据
     * @return List<DownloadingInfo>
     */
    suspend fun getDownloadPrepareList() = database.downloadingInfoDao().getDownloadingList(RetrofitClient.DOWNLOAD_STATE_PREPARE)

    /**
     * 获取历史下载列表
     * @return List<DownloadingInfo>
     */
    suspend fun getHistoryDownloadList() = database.downloadingInfoDao().getHistoryDownloadList()

    /**
     * 获取下载workid
     * @return List<String>
     */
    suspend fun getDownloadingWorkIdList() = database.downloadingInfoDao().getDownloadingWorkIdList()

    /**
     * 获取下载workid
     * @return List<String>
     */
    suspend fun getDownloadingFileIdList() = database.downloadingInfoDao().getDownloadingFileIdList()

    /**
     * 保存用户信息
     * @param user User
     */
    suspend fun insertUser(user: User) = database.userDao().insertUser(user)

    /**
     * 获取用户信息
     * @param id String
     * @return User
     */
    suspend fun getUserInfo(id: String): User = database.userDao().getUser(id)

    /**
     * 登录
     * @param username String 用户名
     * @param pwd String 密码
     * @param captchaCode String 验证码
     * @return Pair<String, UserDto?> 用户信息 pair.first!=REQUEST_SUCCESS 登录失败
     */
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
                    var ignore: Long = 0
                    var start: Long = 0
                    for (expires in response.headers().toMultimap()["expires"]!!) {
//                        LogUtil.err(this@Repository.javaClass, "max_age=${maxAge}")
//                        if (maxAge.contains("max_age")) {
//
//                            //查找max_age位置
//                            val rangeStartPos = maxAge.indexOf("max_age")
//                            val rangeEndPos = maxAge.indexOf(",", rangeStartPos)
//
//                            val endPos = if (rangeEndPos == -1) maxAge.length - 1 else rangeEndPos
//                            val startPos = maxAge.indexOf(":", rangeStartPos)
//                            if (startPos != -1) {
//                                valid = maxAge.substring(startPos + 1, endPos).toLong()
//                            }
//                        }
                        ignore = ToolUtils.utcToLocal(expires, ToolUtils.DATE_TYPE_GMT).time
//
                    }
                    for (date in response.headers().toMultimap()["date"]!!) {

                        start = ToolUtils.utcToLocal(date, ToolUtils.DATE_TYPE_GMT).time
//
                    }

                    body()?.data?.let {

                        database.cookieDao().clear()
                        database.cookieDao().insertUserCookie(UserCookie(it.id,
                                it.nickname,
                                this.headers().toMultimap()["set-cookie"]?.get(0),
                                if (this.headers().toMultimap()["set-cookie"]?.size!! > 1) this.headers().toMultimap()["set-cookie"]?.get(1) else ""
                        ))

                        val sharedPref = SharePreferenceUtils.getSharePreference(context)
                        with(sharedPref.edit()) {
                            putBoolean(SharePreferenceUtils.SP_KEY_LOGIN, true)
                            putString(SharePreferenceUtils.SP_KEY_UID, it.id)
                            putLong(SharePreferenceUtils.SP_KEY_LOGIN_TIME, SystemClock.uptimeMillis())
                            putLong(SharePreferenceUtils.SP_KEY_COOKIE_VALID, (ignore - start) / 1000)
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


    /**
     * 获取网盘文件
     * @return Pair<String, DirectoryDto?> 网盘文件信息
     */
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


    /**
     * 获取文件夹下内容
     *
     * @param path String 文件夹所在路径
     * @return Pair<String, DirectoryDto?> 文件信息
     */
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

    /**
     * 获取下载链接
     *
     * @param id String 代下载文件id
     * @return Pair<String, String> 文件下载链接
     */
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
    /**
     * 下载文件
     *
     * @param url String 文件下载路径
     * @return ResponseBody 字节流
     */
    suspend fun downloadFile(url: String): ResponseBody {
        return retrofit.create(WebService::class.java)
                .downloadFile(url)
    }


    /**
     * 获取文件详细信息
     *
     * @param id String 文件id
     * @param isFolder Boolean 是否为文件夹
     * @param traceRoot Boolean
     * @return Pair<String, FileInfoDto?> 文件信息
     */
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