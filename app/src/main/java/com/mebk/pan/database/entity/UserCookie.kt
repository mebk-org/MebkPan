package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cookie_table")
data class UserCookie(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "nikeName") val name: String?,
        @ColumnInfo(name = "cookie1") val cookie1: String?,
        @ColumnInfo(name = "cookie2") val cookie2: String?
)