package com.mebk.pan.database.daos

import androidx.room.*
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.FileUpdateDownloadClient
import com.mebk.pan.utils.RetrofitClient

@Dao
interface FileDao {

    /**
     * 获取文件
     * @return List<File>
     */
    @Query("SELECT * FROM file_table WHERE path=:path ORDER BY type,date")
    //TODO 根据时间排序
    suspend fun getFile(path: String): List<File>

    @Query("SELECT * FROM file_table WHERE type=:type AND path=:path")
    suspend fun getDir(path: String, type: String = "dir"): List<File>

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


    /**
     * 更新文件表的下载链接
     * @param file FileUpdateDownloadClient
     */
    @Update(entity = File::class)
    suspend fun updateDownloadClient(file: FileUpdateDownloadClient)

    /**
     * 通过id删除文件
     * @param ids List<String>
     */
    @Query("DELETE FROM file_table WHERE id IN (:ids)")
    suspend fun deleteFileById(ids: List<String>)

    /**
     * 删除文件夹
     * @param path List<String> 文件夹路径
     */
    @Query("DELETE FROM file_table WHERE path IN (:path)")
    suspend fun deleteFileByPath(path: List<String>)

}