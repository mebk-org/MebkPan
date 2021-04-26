package com.mebk.pan

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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
import com.mebk.pan.utils.REQUEST_ERR
import com.mebk.pan.utils.REQUEST_SUCCESS
import com.mebk.pan.utils.REQUEST_TIMEOUT
import com.mebk.pan.vm.DirViewModel
import com.mebk.pan.vm.MainViewModel

class DirActivity : AppCompatActivity() {
    private val viewModel by viewModels<DirViewModel>()
    private var dirList = mutableListOf<File>()
    private lateinit var adapter: DirRVAdapter
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    private lateinit var moveBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dir)
        val bundle = intent.getBundleExtra("bundle")


        if (bundle == null) {
            Toast.makeText(this, "参数传递错误,请联系管理员", Toast.LENGTH_SHORT).show()
            finish()
        }
        val fileIdList = bundle!!.getStringArrayList("fileList")
        val dirIdList = bundle.getStringArrayList("dirList")
        val srcDir = bundle.getString("src_dir")

        viewModel.getDir()

        rv = findViewById(R.id.activity_dir_rv)
        sr = findViewById(R.id.activity_dir_sr)
        moveBtn = findViewById(R.id.activity_dir_move_btn)

        sr.setOnRefreshListener {
            sr.isRefreshing = true
            viewModel.getDirNetWork(viewModel.getStackFirst().first, viewModel.getStackFirst().second)
        }

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
            if (sr.isRefreshing) sr.isRefreshing = false
        })

        moveBtn.setOnClickListener {
            viewModel.move(srcDir!!, dirIdList!!, fileIdList!!, viewModel.getUrl())
        }
        viewModel.resultInfo.observe(this, {
            when (it) {
                REQUEST_TIMEOUT -> {
                    Toast.makeText(this, "移动失败，请求网络超时，请重试", Toast.LENGTH_SHORT).show()
                }
                REQUEST_ERR -> {
                    Toast.makeText(this, "未知错误，请联系管理员\n $it", Toast.LENGTH_LONG).show()
                }
                REQUEST_SUCCESS -> {
                    Toast.makeText(this, "移动成功", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            }
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

