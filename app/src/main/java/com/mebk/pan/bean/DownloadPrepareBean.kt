package com.mebk.pan.bean

import com.mebk.pan.dtos.DirectoryDto

data class DownloadPrepareBean(
        val file: DirectoryDto.Object,
        var client: String?,
        var state: Int?
)