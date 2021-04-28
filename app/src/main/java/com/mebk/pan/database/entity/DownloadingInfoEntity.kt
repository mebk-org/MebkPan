package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import java.util.*

/**
 * 下载列表
 */

@Entity(tableName = "downloading_info_table")
class DownloadingInfoEntity(

        @ColumnInfo var fileId: String,

        @ColumnInfo var name: String,

        //本地路径
        @ColumnInfo var path: String,

        @ColumnInfo var client: String,

        @ColumnInfo var size: Long,

        @ColumnInfo var type: String,

        @ColumnInfo var date: Long,

        @ColumnInfo var state: Int,

        @ColumnInfo var progress: Int,

        @ColumnInfo var workID: String,

        ) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}


