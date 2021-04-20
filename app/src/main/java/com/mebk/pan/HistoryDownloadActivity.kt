package com.mebk.pan

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mebk.pan.aa.DownloadRvAdapter
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.vm.HistoryDownloadViewModel

class HistoryDownloadActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: DownloadRvAdapter
    private var list = mutableListOf<DownloadingInfo>()
    private val viewModel by viewModels<HistoryDownloadViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_histroy_download)
        rv = findViewById(R.id.fragment_history_download_rv)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        adapter = DownloadRvAdapter(list, this)
        rv.adapter = adapter

        viewModel.downloadListInfo.observe(this, {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }
}