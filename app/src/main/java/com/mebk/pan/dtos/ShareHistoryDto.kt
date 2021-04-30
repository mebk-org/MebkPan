package com.mebk.pan.dtos

data class ShareHistoryDto(
        val code: Int,
        val data: Data,
        val msg: String
) {

    data class Data(
            val items: List<Item>,
            val total: Int,
            val user: User
    )

    data class Item(
            val create_date: String,
            val downloads: Int,
            val expire: Long,
            val is_dir: Boolean,
            val key: String,
            val password: String,
            val preview: Boolean,
            val remain_downloads: Int,
            val score: Int,
            val source: Source,
            val views: Int
    )

    data class User(
            val date: String,
            val group: String,
            val id: String,
            val nick: String
    )

    data class Source(
            val name: String,
            val size: Long
    )
}
