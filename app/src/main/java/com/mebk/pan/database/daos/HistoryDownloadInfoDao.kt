package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.HistoryDownloadInfo
import kotlinx.coroutines.flow.Flow


@Dao
interface HistoryDownloadInfoDao {

    @Query("SELECT * FROM history_download_info_table")
    fun getDownloadInfo(): Flow<List<HistoryDownloadInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadFile(file: HistoryDownloadInfo)

    @Update
    suspend fun updateDownloadFile(file: HistoryDownloadInfo)

    @Query("DELETE FROM history_download_info_table")
    suspend fun clear()

    @Delete
    suspend fun delete(file: HistoryDownloadInfo)
}