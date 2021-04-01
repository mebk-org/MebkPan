package com.mebk.pan.utils

import java.text.DecimalFormat

class ToolUtils {
    companion object {

        //kb转换
        fun sizeChange(size: Int): String {
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
    }
}
