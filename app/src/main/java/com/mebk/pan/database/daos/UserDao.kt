package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mebk.pan.database.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE ID=:id")
    suspend fun getUser(id: String): UserEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM user_table")
    suspend fun clear()
}