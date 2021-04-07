package com.mebk.pan.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.IOException

class DownloadWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {


    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {


        val fileData = inputData.getString(DOWNLOAD_KEY_OUTPUT_FILE)
                ?: return@withContext Result.failure()

        val file = Json.decodeFromString<DownloadInfo>(fileData)
        val firstUpdate = workDataOf(DOWNLOAD_KEY_PROGRESS to 0)

        setProgress(firstUpdate)
        download(file)
    }

    private suspend fun download(file: DownloadInfo): Result {
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
                        setProgress(workDataOf(DOWNLOAD_KEY_PROGRESS to lastProgress))
                    }
                }
                LogUtil.err(this@DownloadWorker.javaClass, "下载完成")
                file.state = RetrofitClient.DOWNLOAD_STATE_DONE
                (applicationContext as MyApplication).repository.updateDownloadInfo(file)
                return Result.success()
            } catch (e: IOException) {
                file.state = RetrofitClient.DOWNLOAD_STATE_ERR
                (applicationContext as MyApplication).repository.updateDownloadInfo(file)
                LogUtil.err(this@DownloadWorker.javaClass, "下载出错${e}")
                return Result.failure()
            } finally {
                nio.close()
            }
        }
    }

}