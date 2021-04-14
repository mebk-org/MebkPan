package com.mebk.pan.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.*
import java.io.IOException

class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result {


        val downloadClient = inputData.getString(DOWNLOAD_KEY_INPUT_FILE_CLIENT)
                ?: return Result.failure()
        val fileName = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_NAME)
                ?: return Result.failure()
        LogUtil.err(this@DownloadWorker.javaClass, "downloadClient=$downloadClient")
        val date = inputData.getLong(DOWNLOAD_KEY_OUTPUT_FILE_DATE, 0)
        if (date == 0L) return Result.failure()
        val id = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_ID)
                ?: return Result.failure()
        val type = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_TYPE)
                ?: return Result.failure()

        val fileSize = inputData.getLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, 0L)
        if (fileSize == 0L) return Result.failure()
        return download(downloadClient, fileName, fileSize)

    }

    private suspend fun download(client: String, name: String, size: Long): Result {
        val responseBody = (applicationContext as MyApplication).repository.downloadFile(client)
        val nio = NIOUtils(MyApplication.path!! + name)
        LogUtil.err(this@DownloadWorker.javaClass, "contentlen=${responseBody.contentLength()}")
        with(responseBody.byteStream()) {
            val byteArray = ByteArray(1024)
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
                        setProgress(workDataOf(DOWNLOAD_KEY_PROGRESS to (progress * 100).toInt()))
                    } else {
                        setProgress(workDataOf(DOWNLOAD_KEY_PROGRESS to (lastProgress * 100).toInt()))
                    }
                }
                LogUtil.err(this@DownloadWorker.javaClass, "下载完成")
                return Result.success()
            } catch (e: IOException) {
                LogUtil.err(this@DownloadWorker.javaClass, "下载出错${e}")
                return Result.failure()
            } finally {
                responseBody.close()
                nio.close()
            }
        }
    }

}