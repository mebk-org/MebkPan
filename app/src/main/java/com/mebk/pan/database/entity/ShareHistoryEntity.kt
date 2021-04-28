package com.mebk.pan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ShareHistoryEntity(
        @PrimaryKey val key: String,
        val create_date: Long,
        val downloads: Int,
        val expire: Int,
        val is_dir: Boolean,
        val password: String,
        val preview: Boolean,
        val remain_downloads: Int,
        val score: Int,
        val views: Int,
        val name: String,
        val size: Int
)