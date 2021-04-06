package com.mebk.pan.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        val downloadClient = inputData.getString(DOWNLOAD_KEY_INPUT_FILE_CLIENT)
                ?: return@withContext Result.failure()
        val fileName = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_NAME)
                ?: return@withContext Result.failure()

        val fileSize = inputData.getLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, 0L)
        if (fileSize == 0L) return@withContext Result.failure()

        val firstUpdate = workDataOf(DOWNLOAD_KEY_PROGRESS to 0)

        setProgress(firstUpdate)
        download(downloadClient, fileName, fileSize)

    }

    private suspend fun download(url: String, filename: String, size: Long): Result {
        val responseBody = (applicationContext as MyApplication).repository.downloadFile(url)
        val nio = NIOUtils(MyApplication.path!! + filename)
        with(responseBody.byteStream()) {
            val byteArray = ByteArray(65535)
            var lastProgress = 0f
            var current = 0
            var progress: Float
            try {
                while (true) {
                    val len = read(byteArray)

                    if (len < 0) break
                    LogUtil.err(this@DownloadWorker.javaClass, "len=${len}")

                    current += len
                    nio.write(byteArray, len)
                    progress = current.toFloat() / size
                    LogUtil.err(this@DownloadWorker.javaClass, "已下载=${current},进度=${progress},变化=${progress - lastProgress}")
                    if (progress - lastProgress > 0.01F) {
                        lastProgress = progress
                        setProgress(workDataOf(DOWNLOAD_KEY_PROGRESS to lastProgress))
                    }

                }
                LogUtil.err(this@DownloadWorker.javaClass, "下载完成")
                return Result.success()
            } catch (e: IOException) {
                LogUtil.err(this@DownloadWorker.javaClass, "下载出错${e}")
                return Result.failure()
            } finally {
                nio.close()
            }
        }
    }

}