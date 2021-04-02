package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_table")
data class File(
        @PrimaryKey val id: String,
        @ColumnInfo val name: String,
        @ColumnInfo val path: String,
        @ColumnInfo val pic: String,
        @ColumnInfo val size: Long,
        @ColumnInfo val type: String,
        @ColumnInfo val date: Long,
        @ColumnInfo(name="download_client") val downloadClient: String,
)







