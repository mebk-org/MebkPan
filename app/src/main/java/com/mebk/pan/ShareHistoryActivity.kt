package com.mebk.pan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mebk.pan.aa.ShareHistoryRvAdapter
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.ShareHistoryViewModel

class ShareHistoryActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private val list = mutableListOf<ShareHistoryEntity>()
    private lateinit var adapter: ShareHistoryRvAdapter

    private val viewModel by viewModels<ShareHistoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_history)

        viewModel.getShareHistory(MyApplication.uid!!, 1)

        rv = findViewById(R.id.shareHistory_rv)
        adapter = ShareHistoryRvAdapter(this, list)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        viewModel.shareHistoryInfo.observe(this, {
            list.clear()
            list.addAll(it)
            LogUtil.err(this.javaClass,"list=$list")
            adapter.notifyDataSetChanged()
        })
    }
}