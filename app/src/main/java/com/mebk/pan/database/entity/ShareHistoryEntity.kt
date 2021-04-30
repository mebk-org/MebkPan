package com.mebk.pan.database.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ShareHistoryEntity(
        val key: String,
        val create_date: Long,
        val downloads: Int,
        val expire: Long,
        val is_dir: Boolean,
        val password: String,
        val preview: Boolean,
        val remain_downloads: Int,
        val score: Int,
        val views: Int,
        val name: String,
        val size: Long
) : Parcelable