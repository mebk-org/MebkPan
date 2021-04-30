package com.mebk.pan

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
    private var page = 1
    private val viewModel by viewModels<ShareHistoryViewModel>()
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_history)

        viewModel.getShareHistory(page)
        rv = findViewById(R.id.shareHistory_rv)
        sr = findViewById(R.id.shareHistory_sr)

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

        val layoutManager = rv.layoutManager as LinearLayoutManager
        rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                LogUtil.err(this@ShareHistoryActivity.javaClass, "lastVisiblePosition=$lastVisiblePosition,childCount=${recyclerView.childCount}")
                if (lastVisiblePosition == list.size - 1 && !isLoading) {
                    if (!adapter.isEnd) {
                        isLoading = true
                        viewModel.getShareHistory(++page)
                    }
                }
            }
        })

        viewModel.shareHistoryInfo.observe(this, {
            isLoading = false
            if (page == 1) {
                list.clear()
            }
            if (it.isNullOrEmpty()) {
                adapter.isEnd = true
            } else {
                list.addAll(it)
                LogUtil.err(this.javaClass, "list=$list")
            }
            adapter.notifyDataSetChanged()
        })
    }
}