package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.DownloadingInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadingDao {
    @Query("SELECT * FROM downloading_info_table")
    fun getDownloadInfo(): Flow<List<DownloadingInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownloadFile(file: DownloadingInfo)

    @Update
    suspend fun updateDownloadFile(file: DownloadingInfo)

    @Query("DELETE FROM downloading_info_table")
    suspend fun clear()

    @Delete
    suspend fun delete(file: DownloadingInfo)
}