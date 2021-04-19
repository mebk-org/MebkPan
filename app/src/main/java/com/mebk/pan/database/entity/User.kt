package com.mebk.pan.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(

        @PrimaryKey val id: String,
        val anonymous: Boolean,
        val avatar: String,
        val created_at: Int,
        val nickname: String,
        val preferred_theme: String,
        val score: Int,
        val status: Int,
        val user_name: String,
        val groupId: Int,
        val groupName: String,
        val allowSource: Boolean,
        val maxSize: String,
        val saveType: String,
        val upUrl: String
)