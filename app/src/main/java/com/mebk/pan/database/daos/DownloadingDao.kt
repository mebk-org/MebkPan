package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.utils.DOWNLOAD_STATE_DONE
import com.mebk.pan.utils.DOWNLOAD_STATE_DOWNLOADING
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadingDao {
    @Query("SELECT * FROM downloading_info_table")
    fun getDownloadInfo(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state<${DOWNLOAD_STATE_DONE}  ORDER BY id")
    fun getDownloadingList(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state=${DOWNLOAD_STATE_DONE}  ORDER BY id DESC")
    fun getDownloadDoneList(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state=:state  ORDER BY id")
    suspend fun getDownloadingList(state: Int): List<DownloadingInfo>

    @Query("SELECT workID FROM downloading_info_table WHERE state<${DOWNLOAD_STATE_DONE}  ORDER BY id")
    suspend fun getDownloadingWorkIdList(): List<String>

    @Query("SELECT fileId FROM downloading_info_table WHERE state<${DOWNLOAD_STATE_DONE}  ORDER BY id")
    suspend fun getDownloadingFileIdList(): List<String>


    @Query("SELECT * FROM downloading_info_table WHERE state>${DOWNLOAD_STATE_DOWNLOADING}  ORDER BY id")
    suspend fun getHistoryDownloadList(): List<DownloadingInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadFile(file: DownloadingInfo)

    @Query("UPDATE downloading_info_table SET state=:state WHERE fileId=:fileId AND state<${DOWNLOAD_STATE_DONE}")
    suspend fun updateDownloadFileState(fileId: String, state: Int)

    @Query("UPDATE downloading_info_table SET client=:client WHERE fileId=:fileId")
    suspend fun updateDownloadFileClient(fileId: String, client: String)

    @Query("UPDATE downloading_info_table SET workID=:workerId WHERE fileId=:fileId AND state<${DOWNLOAD_STATE_DONE}")
    suspend fun updateDownloadFileWorkerId(fileId: String, workerId: String)

    @Query("UPDATE downloading_info_table SET date=:date WHERE fileId=:fileId AND state<${DOWNLOAD_STATE_DONE}")
    suspend fun updateDownloadDate(fileId: String, date: Long)

    @Query("UPDATE downloading_info_table SET type=:extension WHERE fileId=:fileId AND state<${DOWNLOAD_STATE_DONE}")
    suspend fun updateDownloadFileExtension(fileId: String, extension: String)

    @Query("UPDATE downloading_info_table SET path=:path WHERE fileId=:fileId AND state<${DOWNLOAD_STATE_DONE}")
    suspend fun updateDownloadPath(fileId: String, path: String)

    @Query("DELETE FROM downloading_info_table")
    suspend fun clear()


    @Delete
    suspend fun delete(file: DownloadingInfo)
}