package com.mebk.pan.utils

import com.mebk.pan.bean.DownloadPrepareBean
import java.util.concurrent.ArrayBlockingQueue


class DownloadQueue {
    companion object {
        val instance: DownloadQueue by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            DownloadQueue()
        }
        var isEmpty = false
            private set
        var producerFinish = false
    }

    private val queue = ArrayBlockingQueue<DownloadPrepareBean>(20)


    fun add(item: DownloadPrepareBean) {
        queue.put(item)
        isEmpty = false
    }

    fun get(): DownloadPrepareBean {
        return queue.poll()
    }

    fun clear() {
        while (queue.isEmpty()) {
            queue.poll()
        }
    }

    fun remove(item: DownloadPrepareBean) {
        queue.remove(item)
    }


}
