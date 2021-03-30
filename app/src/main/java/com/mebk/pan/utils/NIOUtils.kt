package com.mebk.pan.utils

import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class NIOUtils(val path: String) {
    private var outputStream: FileOutputStream? = null
    private var fileChannel: FileChannel? = null
    private var byteBuffer: ByteBuffer? = null


    init {
        val file = File(path)
        if (!file.exists()) {
            file.parentFile?.let {
                if (!it.exists()) {
                    file.parentFile!!.mkdir()
                }
            }
            file.createNewFile()
        }
        outputStream = FileOutputStream(file)
        outputStream?.let {
            fileChannel = it.channel
        }
        byteBuffer = ByteBuffer.allocate(4096).order(ByteOrder.nativeOrder())
    }

    /**
     * 写入文件
     * @param bytes ByteArray
     * @return Boolean
     */
    fun write(bytes: ByteArray): Boolean {
        byteBuffer?.let {
            it.clear()
            it.put(bytes)
            fileChannel
        }?.let {
            if (byteBuffer!!.position() != 0) {
                byteBuffer!!.flip()
            }
            it.write(byteBuffer)
            return true
        }
        return false
    }

    /**
     * 关闭nio
     */
    fun close() {
        byteBuffer?.let {
            it.clear()
            fileChannel
        }?.let {
            it.close()
            outputStream
        }?.close()
    }
}