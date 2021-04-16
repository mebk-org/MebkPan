package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.work.WorkInfo
import com.mebk.pan.R
import com.mebk.pan.aa.DownloadRvAdapter
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.utils.DOWNLOAD_KEY_PROGRESS
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.vm.HistoryDownloadViewModel
import com.mebk.pan.vm.MainViewModel

class FragmentHistoryDownload : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: DownloadRvAdapter
    private var listview = mutableListOf<DownloadingInfo>()
    private var downloadingListView = mutableListOf<DownloadingInfo>()
    private var historyListView = mutableListOf<DownloadingInfo>()
    private var downloadDonePos = 0
    private val viewModel by viewModels<HistoryDownloadViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_histroy_download, container, false)

        rv = view.findViewById(R.id.fragment_history_download_rv)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        adapter = DownloadRvAdapter(listview, requireContext())
        rv.adapter = adapter

        return view
    }
}