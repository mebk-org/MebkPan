package com.mebk.pan.dtos

data class UserDto(


        val anonymous: Boolean,
        val avatar: String,
        val created_at: Int,
        val group: Group,
        val id: String,
        val nickname: String,
        val policy: Policy,
        val preferred_theme: String,
        val score: Int,
        val status: Int,
        val tags: List<Any>,
        val user_name: String
) {
    data class Group(
            val allowArchiveDownload: Boolean,
            val allowRemoteDownload: Boolean,
            val allowShare: Boolean,
            val compress: Boolean,
            val id: Int,
            val name: String,
            val shareDownload: Boolean,
            val shareFree: Boolean,
            val webdav: Boolean
    )

    data class Policy(
            val allowSource: Boolean,
            val allowedType: List<Any>,
            val maxSize: String,
            val saveType: String,
            val upUrl: String
    )
}
