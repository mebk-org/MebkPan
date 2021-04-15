package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.utils.RetrofitClient
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadingDao {
    @Query("SELECT * FROM downloading_info_table")
    fun getDownloadInfo(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state<${RetrofitClient.DOWNLOAD_STATE_DONE}  ORDER BY id")
    fun getDownloadingList(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state=${RetrofitClient.DOWNLOAD_STATE_DONE}  ORDER BY id DESC")
    fun getDownloadDoneList(): Flow<List<DownloadingInfo>>

    @Query("SELECT * FROM downloading_info_table WHERE state=:state  ORDER BY id")
    suspend fun getDownloadingList(state: Int): List<DownloadingInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadFile(file: DownloadingInfo)

    @Query("UPDATE downloading_info_table SET state=:state WHERE fileId=:fileId")
    suspend fun updateDownloadFileState(fileId: String, state: Int)

    @Query("UPDATE downloading_info_table SET client=:client WHERE fileId=:fileId")
    suspend fun updateDownloadFileClient(fileId: String, client: String)

    @Query("UPDATE downloading_info_table SET workID=:workerId WHERE fileId=:fileId")
    suspend fun updateDownloadFileWorkerId(fileId: String, workerId: String)


    @Query("DELETE FROM downloading_info_table")
    suspend fun clear()


    @Delete
    suspend fun delete(file: DownloadingInfo)
}