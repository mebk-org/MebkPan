package com.mebk.pan.utils

class ToolUtils {
    companion object {

        fun sizeChange(size: Int): String {
            when {
                size < 1024 -> {
                    // 单位B
                    return "$size B"
                }
                size < 1024 * 1024 -> {
                    //单位kb
                    val kb=size/1024f
                    return "$kb KB"
                }
                size < 1024 * 1024 * 1024 -> {
                    //单位mb
                    val mb=size/1024f/1024f
                    return "$mb MB"
                }
                else -> {
                    //单位gb
                    val gb=size/1024f/1024f/1024f
                    return "$gb GB"
                }
            }
        }

    }
}
