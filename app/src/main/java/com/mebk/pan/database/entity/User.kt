package com.mebk.pan.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
        @PrimaryKey val id: String,
        @ColumnInfo(name = "nikeName") val name: String?,
        @ColumnInfo(name = "cookie1") val cookie1: String?,
        @ColumnInfo(name = "cookie2") val cookie2: String?
)