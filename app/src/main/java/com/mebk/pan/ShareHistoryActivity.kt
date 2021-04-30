package com.mebk.pan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mebk.pan.aa.ShareHistoryRvAdapter
import com.mebk.pan.application.MyApplication
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.ShareHistoryViewModel

class ShareHistoryActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    private val list = mutableListOf<ShareHistoryEntity>()
    private lateinit var adapter: ShareHistoryRvAdapter

    private val viewModel by viewModels<ShareHistoryViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_history)

        viewModel.getShareHistory(MyApplication.uid!!, 1)
        sr = findViewById(R.id.shareHistory_sr)
        rv = findViewById(R.id.shareHistory_rv)

        sr.isRefreshing = true
        adapter = ShareHistoryRvAdapter(this, list)
        adapter.setClickListener {
            val bundle = Bundle().apply {
                putParcelable("file", list[it])
            }
            val intent = Intent().apply {
                setClass(this@ShareHistoryActivity, ShareFileInfoActivity::class.java)
                putExtra("fileInfo", bundle)
            }
            startActivity(intent)
        }
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        viewModel.shareHistoryInfo.observe(this, {
            sr.isRefreshing = false
            list.clear()
            list.addAll(it)
            LogUtil.err(this.javaClass, "list=$list")
            adapter.notifyDataSetChanged()

        })
    }
}