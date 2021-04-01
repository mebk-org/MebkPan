package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mebk.pan.database.entity.User

@Dao
interface UserDao {
    /**
     * 获取用户信息
     * @param id String 用户id
     * @return List<User> 用户信息
     */
    @Query("SELECT * FROM user_table WHERE id=:id ")
    suspend fun getUserCookie(id: String): List<User>

    /**
     * 插入用户，通常情况下只能存在一个用户信息
     * @param user User
     */
    @Insert
    suspend  fun insertUser(user: User)

    /**
     * 删除表中全部数据
     */
    @Query("DELETE FROM user_table")
    suspend fun clear()
}