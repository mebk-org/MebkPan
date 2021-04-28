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
import androidx.work.WorkInfo
import com.mebk.pan.R
import com.mebk.pan.aa.DownloadingRvAdapter
import com.mebk.pan.database.entity.DownloadingInfoEntity
import com.mebk.pan.utils.DOWNLOAD_KEY_PROGRESS
import com.mebk.pan.utils.DOWNLOAD_STATE_DOWNLOADING
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DownloadingViewModel
import com.mebk.pan.vm.MainViewModel
import kotlinx.android.synthetic.main.rv_item_downloading.view.*

class FragmentDownloading : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: DownloadingRvAdapter
    private var list = mutableListOf<DownloadingInfoEntity>()
    private val viewModel by viewModels<DownloadingViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    private var offset = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =inflater.inflate(R.layout.fragment_downloading, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv = view.findViewById(R.id.fragment_downloading_rv)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = DownloadingRvAdapter(list, requireContext())
        rv.adapter = adapter
        viewModel.downloadingListInfo.observe(viewLifecycleOwner, {
            for (info in it) {
                LogUtil.err(this.javaClass, "info=$info")
            }
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        })

        adapter.setOnClickCancelImageViewListener {
            mainViewModel.cancelDownload(list[it].fileId)
        }

        mainViewModel.downloadWorkerInfo.observe(viewLifecycleOwner, {
            when (it.state) {
                WorkInfo.State.RUNNING -> {
                    if (list.isNotEmpty()) {
//                        while ((mainViewModel.getCurrentPos() + offset < list.size)
//                                && (list[mainViewModel.getCurrentPos() + offset].state == RetrofitClient.DOWNLOAD_STATE_CLIENT_ERR)) {
//                            ++offset
//                        }
//                        if (mainViewModel.getCurrentPos() + offset >= list.size) return@observe
//                        if (list[mainViewModel.getCurrentPos() + offset].state != RetrofitClient.DOWNLOAD_STATE_DOWNLOADING) {
//                            list[mainViewModel.getCurrentPos() + offset].state = RetrofitClient.DOWNLOAD_STATE_DOWNLOADING
//                            adapter.notifyDataSetChanged()
//                        }
                        if (list[0].state != DOWNLOAD_STATE_DOWNLOADING) {
                            list[0].state = DOWNLOAD_STATE_DOWNLOADING
                            adapter.notifyDataSetChanged()
                        }
                        val progress = it.progress.getInt(DOWNLOAD_KEY_PROGRESS, 0)
                        LogUtil.err(this.javaClass, "progress=$progress")
                        val viewHolder = rv.findViewHolderForAdapterPosition(0)
                        viewHolder?.let { vh ->
                            vh.itemView.rv_item_history_download_waiting_progress.progress = progress
                        }
                    }
                }
            }
        })


    }
}