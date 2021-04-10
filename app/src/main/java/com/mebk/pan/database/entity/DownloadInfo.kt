package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 下载信息 (包含历史下载)
 */

@Entity(tableName = "download_info_table")
data class DownloadInfo(
        @PrimaryKey val id: String,

        @ColumnInfo val name: String,

        //本地路径
        @ColumnInfo val path: String,

        @ColumnInfo var client: String,

        @ColumnInfo val size: Long,

        @ColumnInfo val type: String,

        @ColumnInfo val date: Long,

        @ColumnInfo var state: Int,

)

