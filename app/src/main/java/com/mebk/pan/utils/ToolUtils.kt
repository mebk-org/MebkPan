package com.mebk.pan.utils

import android.content.Intent
import android.graphics.drawable.Drawable
import com.mebk.pan.R
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


//kb转换
fun sizeChange(size: Long): String {
    val df = DecimalFormat("#.00")
    when {
        size < 1024 -> {
            // 单位B
            return "$size B"
        }
        size < 1024 * 1024 -> {
            //单位kb
            val kb = size / 1024f
            return "${df.format(kb)} KB"
        }
        size < 1024 * 1024 * 1024 -> {
            //单位mb
            val mb = size / 1024f / 1024f
            return "${df.format(mb)} MB"
        }
        else -> {
            //单位gb
            val gb = size / 1024f / 1024f / 1024f
            return "${df.format(gb)} GB"
        }
    }
}


/**
 * 拼接url
 * @param url Array<out String> 需要拼接的字符串
 * @return String 拼接结果为s=[0]+[1]+...
 */
fun splitUrl(vararg url: String): String {
    var path = ""
    for (s in url) {
        path += s
    }
    return path
}

/**
 * 根据文件类型与文件名选择缩略图
 *
 * @param type String
 * @param name String
 * @return Drawable
 */
fun chooseDirectoryThumbnail(type: String, name: String): Int {
    return when {
        type == "dir" -> R.drawable.directory_32
        name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") -> R.drawable.file_zip
        name.endsWith(".txt") -> R.drawable.file_txt
        name.endsWith(".doc") || name.endsWith(".docx") -> R.drawable.file_word
        name.endsWith(".xls") || name.endsWith(".xlsx") -> R.drawable.file_excel
        name.endsWith(".pdf") -> R.drawable.file_pdf
        name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif") -> R.drawable.file_image
        name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".aac") || name.endsWith(".wma") -> R.drawable.file_music
        name.endsWith(".mp4") || name.endsWith(".flv") || name.endsWith(".avi") || name.endsWith(".wmp") -> R.drawable.file_play
        name.endsWith(".bin") -> R.drawable.file_binary
        name.endsWith(".bat") || name.endsWith(".sh") -> R.drawable.file_code
        else -> R.drawable.file_32
    }
}


const val DATE_TYPE_UTC = 0
const val DATE_TYPE_GMT = 1

/**
 * utc时间转成local时间
 * @param time String
 * @param dateType Int
 * @return Date
 */
fun utcToLocal(time: String, dateType: Int): Date {
    var sdf: SimpleDateFormat? = null
    when (dateType) {
        DATE_TYPE_GMT -> {
            sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("GMT")
        }
        DATE_TYPE_UTC -> {
            sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    sdf?.let {
        val utcDate = it.parse(time)
        it.timeZone = TimeZone.getDefault()
        val localTime = it.format(utcDate.time)
        return it.parse(localTime)
    }
    return Date(0L)
}

/**
 * utc时间转成local时间
 * @param time Long
 * @param dateType Int
 * @return Date
 */
fun utcToLocal(time: Long, dateType: Int): Date {
    var sdf: SimpleDateFormat? = null
    when (dateType) {
        DATE_TYPE_GMT -> {
            sdf = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("GMT")
        }
        DATE_TYPE_UTC -> {
            sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    sdf?.let {
        return it.parse(it.format(time))
    }
    return Date(0L)
}
/**
 * 时间戳转string
 * @param time Long
 * @return String
 */
fun timeStamp2String(time: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return sdf.format(time)
}

fun string2timeStamp(time: String): Long {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    return sdf.parse(time).time
}

/**
 * 获取文件类型
 * @param filename String
 */
fun getFileMimeType(filename: String): String {
    val pos = filename.lastIndexOf(".")
    if (pos == -1) return ""
    return filename.substring(pos)
}