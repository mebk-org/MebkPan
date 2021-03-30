package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileUpdateDownloadClient(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "download_client") val downloadClient: String,
)