package com.mebk.pan.database.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mebk.pan.database.entity.UserCookie

@Dao
interface CookieDao {
    /**
     * 获取用户信息
     * @param id String 用户id
     * @return List<User> 用户信息
     */
    @Query("SELECT * FROM cookie_table WHERE id=:id ")
    suspend fun getUserCookie(id: String): List<UserCookie>

    /**
     * 插入用户，通常情况下只能存在一个用户信息
     * @param userCookie User
     */
    @Insert
    suspend  fun insertUserCookie(userCookie: UserCookie)

    /**
     * 删除表中全部数据
     */
    @Query("DELETE FROM cookie_table")
    suspend fun clear()
}