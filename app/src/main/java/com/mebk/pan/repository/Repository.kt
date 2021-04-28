package com.mebk.pan.repository

import android.content.Context
import android.os.SystemClock
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.DataBase
import com.mebk.pan.database.entity.*
import com.mebk.pan.dtos.*
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
    suspend fun getUserCookie(uid: String): List<UserCookieEntity> = database.cookieDao().getUserCookie(uid)


    /**
     * 从本地获取文件
     */
    suspend fun getDir(path: String): List<FileEntity> = database.fileDao().getDir(path)

    /**
     * 从本地获取文件夹
     */
    suspend fun getFile(path: String = "/"): List<FileEntity> = database.fileDao().getFile(path)

    /**
     * 更新本地下载链接
     */
    suspend fun updateDownloadClient(file: FileUpdateDownloadClientEntity) = database.fileDao().updateDownloadClient(file)

    /**
     * 修改文件路径
     * @param id List<String> id列表
     * @param path String 路径
     */
    suspend fun changePath(id: List<String>, path: String) = database.fileDao().changePath(id, path)


    /**
     * 获取下载列表
     */
    fun getDownloadingInfo(): Flow<List<DownloadingInfoEntity>> = database.downloadingInfoDao().getDownloadInfo()

    /**
     * 存储下载列表
     */
    suspend fun addDownloadingInfo(file: DownloadingInfoEntity) = database.downloadingInfoDao().insertDownloadFile(file)

    /**
     * 删除下载列表
     */
    suspend fun deleteDownloadingInfo(file: DownloadingInfoEntity) = database.downloadingInfoDao().delete(file)

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
    suspend fun getDownloadPrepareList() = database.downloadingInfoDao().getDownloadingList(DOWNLOAD_STATE_PREPARE)

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
    suspend fun insertUser(user: UserEntity) = database.userDao().insertUser(user)

    /**
     * 获取用户信息
     * @param id String
     * @return User
     */
    suspend fun getUserInfo(id: String): UserEntity = database.userDao().getUser(id)

    /**
     * 插入文件
     * @param file File
     */
    suspend fun addFile(file: FileEntity) = database.fileDao().insertFile(file)

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
                        ignore = utcToLocal(expires, DATE_TYPE_GMT).time
//
                    }
                    for (date in response.headers().toMultimap()["date"]!!) {

                        start = utcToLocal(date, DATE_TYPE_GMT).time
//
                    }

                    body()?.data?.let {

                        database.cookieDao().clear()
                        database.cookieDao().insertUserCookie(UserCookieEntity(it.id,
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
                    pair = Pair(REQUEST_SUCCESS, body()?.data)
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(REQUEST_TIMEOUT, null)
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
                        for (file in it.objects) {
                            with(database.fileDao()) {
                                insertFile(FileEntity(
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
                    pair = Pair(REQUEST_SUCCESS, body()?.data)
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(REQUEST_TIMEOUT, null)
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
                    .getInternalFile(splitUrl(API_DIRECTORY, path))
            LogUtil.err(this::class.java, response.toString())
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        pair = Pair(REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(REQUEST_TIMEOUT, null)
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
        var pair = Pair("", "")
        try {
            val response = retrofit.create(WebService::class.java)
                    .getDownloadFileClient(splitUrl(API_DOWNLOAD_CLIENT, id))
            LogUtil.err(this::class.java, response.toString())
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        updateDownloadClient(FileUpdateDownloadClientEntity(id, it))
                        pair = Pair(REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, "")
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(REQUEST_TIMEOUT, "")
        } catch (e: java.lang.Exception) {
            pair = Pair(e.toString(), "")
        }

        return pair
    }

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
                    .getFileInfo(splitUrl(API_FILE_INFO, id), traceRoot, isFolder)
            with(response) {
                if (body()?.code == 0) {
                    body()?.data?.let {
                        pair = Pair(REQUEST_SUCCESS, it)
                    }
                } else {
                    pair = Pair(body()?.msg!!, null)
                }
            }
        } catch (e: SocketTimeoutException) {
            pair = Pair(REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            pair = Pair(e.toString(), null)
        }
        return pair
    }

    /**
     * 删除文件
     * @param pair Pair<List<String>, List<String>> first=文件id，second=文件夹id
     * @param pathArr List<String> 如果删除文件夹，则为文件夹路径，否则为空数组
     * @return Pair<String, DeleteDto?>
     */
    suspend fun deleteFile(pair: Pair<List<String>, List<String>>, pathArr: List<String>): Pair<String, ActionDto?> {
        var idArr = JsonArray()
        var dirsArr = JsonArray()
        pair.first.forEach {
            idArr.add(it)
        }
        pair.second.forEach {
            dirsArr.add(it)
        }
        val jsonObj = JsonObject()

        var result = Pair<String, ActionDto?>("", null)
        jsonObj.add("items", idArr)
        jsonObj.add("dirs", dirsArr)
        val requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonObj.toString())
        try {

            val response = retrofit.create(WebService::class.java).deleteFile(requestBody)
            with(response) {
                body()?.let {
                    if (it.code == 0) {
                        result = Pair(REQUEST_SUCCESS, body())
                        if (pair.first.isNotEmpty()) {
                            database.fileDao().deleteFileById(pair.first)
                        }

                        if (pair.second.isNotEmpty()) {
                            database.fileDao().deleteFileById(pair.second)
                            database.fileDao().deleteFileByPath(pathArr)
                        }
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            result = Pair(REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            result = Pair(e.toString(), null)
        }
        return result
    }

    /**
     * 移动文件
     * @param srcDir String 文件当前路径
     * @param dirList List<String> 需要移动的文件列表
     * @param fileList List<String> 需要移动的文件夹列表
     * @param dst String  移动的目标路径
     * @return Pair<String, ActionDto?>
     */
    suspend fun moveFile(srcDir: String, dirList: List<String>, fileList: List<String>, dst: String): Pair<String, ActionDto?> {
        val idArr = JsonArray()
        val dirsArr = JsonArray()
        fileList.forEach {
            idArr.add(it)
        }
        dirList.forEach {
            dirsArr.add(it)
        }

        val srcObj = JsonObject()
        srcObj.add("dirs", dirsArr)
        srcObj.add("items", idArr)
        val jsonObj = JsonObject()
        jsonObj.add("src", srcObj)
        jsonObj.addProperty("action", "move")
        jsonObj.addProperty("src_dir", srcDir)
        jsonObj.addProperty("dst", dst)
        var result = Pair<String, ActionDto?>("", null)
        val requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonObj.toString())
        try {

            val response = retrofit.create(WebService::class.java).moveFile(requestBody)
            with(response) {
                body()?.let {
                    if (it.code == 0) {
                        result = Pair(REQUEST_SUCCESS, body())
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            result = Pair(REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            result = Pair(e.toString(), null)
        }
        return result
    }

    /**
     * 文件分享
     * @param id String 文件id
     * @param isDir Boolean 是否为文件夹
     * @param pwd String 分享密码
     * @param downloads Int 下载次数，-1为不限
     * @param expire Long 有效期 单位秒
     * @param preview Boolean 是否支持预览
     * @param score Int 积分，暂未开通默认为0
     * @return Pair<String, ShareDao>
     */
    suspend fun shareFile(id: String, isDir: Boolean, pwd: String, downloads: Int, expire: Long, preview: Boolean, score: Int = 0): Pair<String, ShareDto?> {
        val jsonObject = JsonObject()
        jsonObject.addProperty("id", id)
        jsonObject.addProperty("is_dir", isDir)
        jsonObject.addProperty("password", pwd)
        jsonObject.addProperty("downloads", downloads)
        jsonObject.addProperty("expire", expire)
        jsonObject.addProperty("score", score)
        jsonObject.addProperty("preview", preview)
        val requestBody = RequestBody.create(MediaType.parse(CONTENT_TYPE_JSON), jsonObject.toString())
        var result = Pair<String, ShareDto?>("", null)
        try {
            val response = retrofit.create(WebService::class.java).shareFile(requestBody)
            with(response) {
                body()?.let {
                    if (it.code == 0) {
                        result = Pair(REQUEST_SUCCESS, body())
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            result = Pair(REQUEST_TIMEOUT, null)
        } catch (e: java.lang.Exception) {
            result = Pair(e.toString(), null)
        }
        return result
    }

    suspend fun shareHistory(id:String,page:Int){

    }
}