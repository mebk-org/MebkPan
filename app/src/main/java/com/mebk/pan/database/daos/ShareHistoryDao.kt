package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mebk.pan.database.entity.ShareHistoryEntity

@Dao
interface ShareHistoryDao {
    @Query("SELECT * FROM share_history_table")
    suspend fun getShareHistory(): List<ShareHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShareHistory(shareHistoryEntity: ShareHistoryEntity)

    @Query("DELETE FROM share_history_table")
    suspend fun clear()
}