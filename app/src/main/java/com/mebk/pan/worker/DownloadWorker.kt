package com.mebk.pan.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.HistoryDownloadInfo
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

        val date = inputData.getLong(DOWNLOAD_KEY_OUTPUT_FILE_DATE, 0)
        if (date == 0L) return@withContext Result.failure()
        val id = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_ID)
                ?: return@withContext Result.failure()
        val type = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE_TYPE)
                ?: return@withContext Result.failure()

        val fileSize = inputData.getLong(DOWNLOAD_KEY_INPUT_FILE_SIZE, 0L)
        if (fileSize == 0L) return@withContext Result.failure()

        var file = HistoryDownloadInfo(id, fileName, "", downloadClient, fileSize, type, date, RetrofitClient.DOWNLOAD_STATE_WAIT)

        download(file)

    }

    private suspend fun download(file: HistoryDownloadInfo): Result {
        val responseBody = (applicationContext as MyApplication).repository.downloadFile(file.client)
        val nio = NIOUtils(MyApplication.path!! + file.name)
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
                    progress = current.toFloat() / file.size
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
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                LogUtil.err(this@DownloadWorker.javaClass, "下载出错${e}")
                return Result.failure()
            } finally {
                nio.close()
            }
        }
    }

}