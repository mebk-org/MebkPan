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

//        mainViewModel.downloadingList.observe(viewLifecycleOwner, {
//            downloadingListView.clear()
//            downloadingListView.addAll(it)
//            listview.clear()
//            if (downloadingListView.isNotEmpty()) {
//                listview.add(DownloadingInfo("", "下载中", "", ",", 0, "tag", 0, 0, 0, ""))
//                listview.addAll(downloadingListView)
//            }
//            if (historyListView.isNotEmpty()) {
//                listview.add(DownloadingInfo("", "已完成", "", ",", 0, "tag", 0, 0, 0, ""))
//                listview.addAll(historyListView)
//            }
//        })

//        viewModel.downloadingListInfo.observe(viewLifecycleOwner, {
//            downloadingListView.clear()
//            if (it.isNotEmpty()) {
//                downloadingListView.add(DownloadingInfo("", "下载中", "", ",", 0, "tag", 0, 0, 0, ""))
//                downloadingListView.addAll(it)
//            }
//            listview.clear()
//            listview.addAll(downloadingListView)
//            listview.addAll(historyListView)
//            adapter.notifyDataSetChanged()
//        })
//        viewModel.downloadingDoneInfo.observe(viewLifecycleOwner, {
//            if (it.isNotEmpty()) {
//                historyListView = it.toMutableList()
//                if (historyListView.isNullOrEmpty()) {
//                    historyListView.add(0, DownloadingInfo("", "已完成", "", ",", 0, "tag", 0, 0, 0, ""))
//                }
//            }
//            listview.clear()
//            listview.addAll(downloadingListView)
//            listview.addAll(historyListView)
//            adapter.notifyDataSetChanged()
//        })
//
//        mainViewModel.downloadWorkerInfo.observe(viewLifecycleOwner, {
//            when (it.state) {
//                WorkInfo.State.RUNNING -> {
//                    if (listview.isNotEmpty()) {
//                        if (listview[mainViewModel.getCurrentPos()+1].state != RetrofitClient.DOWNLOAD_STATE_DOWNLOADING) {
//                            listview[mainViewModel.getCurrentPos()+1].state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
//                            adapter.notifyDataSetChanged()
//                        }
//                        val progress = it.progress.getInt(DOWNLOAD_KEY_PROGRESS, 0)
//                        LogUtil.err(this.javaClass, "progress=$progress")
//                        val viewHolder = rv.findViewHolderForAdapterPosition(mainViewModel.getCurrentPos()+1)
//                        viewHolder?.let { vh ->
//                            vh.itemView.rv_item_history_download_waiting_progress.progress = progress
//                        }
//                    }
//                }
//            }
//        })
//        mainViewModel.downloadWorkInfo.observe(viewLifecycleOwner, {
//
////            for (info in it) {
////                LogUtil.err(this.javaClass, "info=$info")
////                LogUtil.err(this.javaClass, "循环结束")
////            }
////            when (it.state) {
////                WorkInfo.State.ENQUEUED -> {
////                    if (listview.isNotEmpty()) {
////                        listview[mainViewModel.currentPos].state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
////                        adapter.notifyDataSetChanged()
////                    }
////                }
////                WorkInfo.State.RUNNING -> {
////                    if (listview.isNotEmpty()) {
////                        if (listview[mainViewModel.currentPos].state != RetrofitClient.DOWNLOAD_STATE_DOWNLOADING) {
////                            listview[mainViewModel.currentPos].state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
////                            adapter.notifyDataSetChanged()
////                        }
////                        val progress = it.progress.getInt(DOWNLOAD_KEY_PROGRESS, 0)
////                        LogUtil.err(this.javaClass, "progress=$progress")
////                        val viewHolder = rv.findViewHolderForAdapterPosition(mainViewModel.currentPos)
////                        viewHolder?.let { vh ->
////                            vh.itemView.rv_item_history_download_waiting_progress.progress = progress
////                        }
////                    }
////                }
////            }
//
//        })


        return view
    }
}