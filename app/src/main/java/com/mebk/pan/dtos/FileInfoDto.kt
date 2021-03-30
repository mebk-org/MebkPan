package com.mebk.pan.dtos

data class FileInfoDto(
    val code: Int,
    val data: Data,
    val msg: String
){
    data class Data(
            val child_file_num: Int,
            val child_folder_num: Int,
            val created_at: String,
            val path: String,
            val policy: String,
            val query_date: String,
            val size: Int,
            val updated_at: String
    )
}