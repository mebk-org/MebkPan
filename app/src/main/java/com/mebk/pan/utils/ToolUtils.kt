package com.mebk.pan.utils

import android.graphics.drawable.Drawable
import com.mebk.pan.R
import java.text.DecimalFormat

class ToolUtils {
    companion object {

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
    }
}