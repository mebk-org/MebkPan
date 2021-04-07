package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.DownloadInfo
import kotlinx.coroutines.flow.Flow


@Dao
interface DownloadInfoDao {

    @Query("SELECT * FROM download_info_table")
    fun getDownloadInfo(): Flow<List<DownloadInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadFile(file: DownloadInfo)

    @Update
    suspend fun updateDownloadFile(file: DownloadInfo)

    @Query("DELETE FROM download_info_table")
    suspend fun clear()

    @Delete
    suspend fun delete(file: DownloadInfo)
}