package com.mebk.pan.dtos

data class DirectoryDto(
        val code: Int,
        val data: Data,
        val msg: String
) {
    data class Data(
            val objects: List<Object>,
            val parent: String
    )

    data class Object(
            val date: String,
            val id: String,
            val name: String,
            val path: String,
            val pic: String,
            val size: Int,
            val type: String
    )
}