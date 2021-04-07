package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mebk.pan.R
import com.mebk.pan.aa.HistoryDownloadRvAdapter
import com.mebk.pan.database.entity.DownloadInfo
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.MainViewModel

class FragmentHistoryDownload : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: HistoryDownloadRvAdapter
    private var listview = mutableListOf<DownloadInfo>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_histroy_download, container, false)

        rv = view.findViewById(R.id.fragment_history_download_rv)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = HistoryDownloadRvAdapter(listview, requireActivity())
        rv.adapter = adapter

        mainViewModel.downloadListInfo.observe(viewLifecycleOwner, Observer {
            listview.clear()
            listview.addAll(it)
            for (n in it) {
                LogUtil.err(this.javaClass, "file=${n.toString()}")
            }
            adapter.notifyDataSetChanged()
        })

        return view
    }
}