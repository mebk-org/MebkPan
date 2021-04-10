package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.mebk.pan.R
import com.mebk.pan.aa.HistoryDownloadRvAdapter
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.utils.DOWNLOAD_KEY_PROGRESS
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DownloadViewModel
import com.mebk.pan.vm.HistoryDownloadViewModel
import com.mebk.pan.vm.MainViewModel
import kotlinx.android.synthetic.main.rv_item_history_download_waiting.view.*

class FragmentHistoryDownload : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: HistoryDownloadRvAdapter
    private var listview = mutableListOf<DownloadInfo>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private val downloadVewModel by viewModels<HistoryDownloadViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_histroy_download, container, false)

        rv = view.findViewById(R.id.fragment_history_download_rv)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = HistoryDownloadRvAdapter(listview, requireActivity())
        rv.adapter = adapter

        mainViewModel.downloadListInfo.observe(viewLifecycleOwner, {

        })

        mainViewModel.downloadWorkInfo.observe(viewLifecycleOwner, {
            it.forEach {
                if (it.state == WorkInfo.State.RUNNING) {
                    val progress = it.progress.getInt(DOWNLOAD_KEY_PROGRESS, 0)
                    val viewHolder = rv.findViewHolderForAdapterPosition(0)
                    viewHolder?.let { vh ->
                        vh.itemView.rv_item_history_download_waiting_progress.progress = progress
                    }
                }
            }
        })


        downloadVewModel.downloadInfo.observe(viewLifecycleOwner, {
            listview.clear()
            listview.addAll(it)
            adapter.notifyDataSetChanged()

        })

        return view
    }
}