package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mebk.pan.R
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.vm.DirectoryViewModel
import com.mebk.pan.vm.MainViewModel
import kotlinx.android.synthetic.main.fragment_directory.*

class FragmentDirectory : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    private val viewModel by viewModels<DirectoryViewModel>()
    private val mainViewModel by activityViewModels<MainViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_directory, container, false)
        val list: MutableList<DirectoryDto.Object> = mutableListOf()

        rv = view.findViewById(R.id.fragment_directory_rv)
        sr = view.findViewById(R.id.fragment_directory_sr)
        sr.setProgressViewEndTarget(true, 300)
        viewModel.directory()

        sr.isRefreshing = true

        val adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        viewModel.directoryInfo.observe(viewLifecycleOwner, Observer {
            LogUtil.err(this::class.java, "更新列表")
            list.clear()
            list.addAll(it)
            adapter?.notifyDataSetChanged()
            sr.isRefreshing = false
        })

        viewModel.requestInfo.observe(viewLifecycleOwner, Observer {
            if (it != RetrofitClient.REQUEST_SUCCESS) {
                Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
                sr.isRefreshing = false
                list.removeAt(0)
                adapter?.notifyItemRemoved(0)
            }
        })


        sr.setOnRefreshListener {
            val refreshDto = DirectoryDto.Object(viewModel.lastRefreshTimeInfo.value!!, "0", "正在刷新...", "", "", 0, "refresh")
            list.add(0, refreshDto)
            rv.scrollToPosition(0)
            adapter?.notifyItemInserted(0)
            sr.isRefreshing = true
            viewModel.directory(true)
        }

        adapter?.let { directoryRvAdapter ->
            directoryRvAdapter.setOnClickListener {
                if (sr.isRefreshing) {
                    Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                } else {
                    val bundle = Bundle()
                    bundle.putString("path", viewModel.directoryInfo.value!![it].path)
                    when (viewModel.directoryInfo.value!![it].type) {
                        "dir" -> {
                            bundle.putString("name", viewModel.directoryInfo.value!![it].name)
                            findNavController().navigate(R.id.action_fragment_directory_to_fragment_internal_file, bundle)
                        }
                        else -> {
                            bundle.putString("id", viewModel.directoryInfo.value!![it].id)
                            findNavController().navigate(R.id.action_fragment_directory_to_downloadFragment, bundle)
                        }
                    }

                }
            }
            directoryRvAdapter.setOnLongClickListener {
                if (sr.isRefreshing) {
                    Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
                } else {
//                    Bundle().apply {
//                        putString("id", viewModel.directoryInfo.value!![it].id)
//                        putString("type", viewModel.directoryInfo.value!![it].type)
//                        putString("name", viewModel.directoryInfo.value!![it].name)
//                        findNavController().navigate(R.id.action_fragment_directory_to_fragmentFileInfo, this)
//                    }
                    mainViewModel.changeFileOperator()
                }
            }
        }

        //拦截返回事件
        val callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            mainViewModel.changeFileOperator()
        }

        mainViewModel.isFileOperator.observe(viewLifecycleOwner, Observer {
            callBack.isEnabled = it

            adapter?.isFileOperator = it
            adapter?.notifyItemRangeChanged(0, list.size)


        })

        return view
    }
}