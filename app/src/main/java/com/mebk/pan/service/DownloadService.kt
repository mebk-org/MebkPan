package com.mebk.pan.service

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.work.ListenableWorker
import androidx.work.workDataOf
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.DOWNLOAD_KEY_PROGRESS
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.NIOUtils
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.ScheduledExecutorService

class DownloadService : Service() {
    val application: MyApplication = getApplication() as MyApplication
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }

    inner class DownloadBinder : Binder() {
        suspend fun startDownload(url: String, name: String, size: Long) = withContext(Dispatchers.IO) {
            val responseBody = (applicationContext as MyApplication).repository.downloadFile(url)
            val nio = NIOUtils(MyApplication.path!! + name)
            with(responseBody.byteStream()) {
                val byteArray = ByteArray(65535)
                var lastProgress = 0f
                var current = 0
                var progress: Float
                try {
                    while (true) {
                        val len = read(byteArray)

                        if (len < 0) break
                        current += len
                        //应保证写入的字节与接收到的字节大小一致
                        nio.write(byteArray, len)
                        progress = current.toFloat() / size
                        if (progress - lastProgress > 0.01F) {
                            lastProgress = progress
                        } else {
                        }
                    }
                    responseBody.close()
                    responseBody.contentLength()
                    LogUtil.err(this@DownloadBinder.javaClass, "下载完成")
                } catch (e: IOException) {
                    LogUtil.err(this@DownloadBinder.javaClass, "下载出错${e}")
                } finally {
                    nio.close()
                }
            }
        }

        suspend fun cancelDownload() {

        }
    }
}