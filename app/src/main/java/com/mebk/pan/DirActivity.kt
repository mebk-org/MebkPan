package com.mebk.pan

import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mebk.pan.aa.DirRVAdapter
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DirViewModel
import com.mebk.pan.vm.MainViewModel

class DirActivity : AppCompatActivity() {
    private val viewModel by viewModels<DirViewModel>()

    private var dirList = mutableListOf<File>()
    private lateinit var adapter: DirRVAdapter
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir)
        viewModel.getDir()

        rv = findViewById(R.id.activity_dir_rv)
        sr = findViewById(R.id.activity_dir_sr)

//        sr.setOnRefreshListener {
//            sr.isRefreshing=true
//            viewModel.getDir()
//        }

        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = DirRVAdapter(this, dirList)

        rv.adapter = adapter

        adapter.setClickListener {
            viewModel.getDir(dirList[it].path, dirList[it].name)
        }


        viewModel.dirInfo.observe(this, {
            dirList.clear()
            dirList.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

    override fun onBackPressed() {
        if (viewModel.stackSize.value != 1) {
            viewModel.back()
        } else {
            finish()
        }
    }
}

