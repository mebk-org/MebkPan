package com.mebk.pan.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mebk.pan.database.daos.DownloadInfoDao
import com.mebk.pan.database.daos.FileDao
import com.mebk.pan.database.daos.UserDao
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.database.entity.File
import com.mebk.pan.database.entity.User

@Database(entities = [User::class, File::class, DownloadInfo::class], version = 1)
abstract class DataBase() : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun fileDao(): FileDao
    abstract fun downloadInfoDao(): DownloadInfoDao

    companion object {
        //通过单例模式获取实例
        @Volatile
        private var INSTANCE: DataBase? = null

        fun getDatabase(context: Context): DataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        DataBase::class.java,
                        "mebk_pan_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }


}