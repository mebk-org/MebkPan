package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mebk.pan.database.entity.File

@Dao
interface FileDao {

    /**
     * 获取文件
     * @return List<File>
     */
    @Query("SELECT * FROM file_table")
    //TODO 根据时间排序
    suspend fun getFile(): List<File>

    /**
     * 插入文件
     * @param file File
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: File)

    /**
     * 删除表中全部数据
     */
    @Query("DELETE FROM file_table")
    suspend fun clear()
}