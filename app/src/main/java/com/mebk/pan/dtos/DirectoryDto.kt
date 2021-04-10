package com.mebk.pan.dtos

data class DirectoryDto(
        val objects: List<Object>,
        val parent: String
) {
    data class Object(
            val date: String,
            val id: String,
            val name: String,
            val path: String,
            val pic: String,
            val size: Long,
            val type: String
    )
}

