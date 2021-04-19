package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.User

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE ID=:id")
    suspend fun getUser(id: String): User

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM user_table")
    suspend fun clear()
}