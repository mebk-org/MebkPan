package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mebk.pan.R
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DirectoryViewModel
import kotlinx.android.synthetic.main.fragment_directory.*

class FragmentDirectory : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    private val viewModel by viewModels<DirectoryViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_directory, container, false)
        var list: MutableList<DirectoryDto.Object> = mutableListOf()

        rv = view.findViewById(R.id.fragment_directory_rv)
        sr = view.findViewById(R.id.fragment_directory_sr)
        sr.setProgressViewEndTarget(true, 300)
        viewModel.directory()

        sr.isRefreshing = true

        var adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        viewModel.directoryInfo.observe(viewLifecycleOwner, Observer {
            list.clear()
            list.addAll(it)
            adapter?.notifyDataSetChanged()
            sr.isRefreshing = false
        })


        sr.setOnRefreshListener {
            val refreshDto = DirectoryDto.Object(viewModel.lastRefreshTimeInfo.value!!, "0", "正在刷新...", "", "", 0, "refresh")
            list.add(0, refreshDto)
            adapter?.notifyItemInserted(0)
            sr.isRefreshing = true
            viewModel.directory()
        }

        adapter?.setOnClickListener {
            if (sr.isRefreshing) {
                Toast.makeText(context, "正在刷新，请稍后", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle()
                with(bundle) {
                    putString("name", viewModel.directoryInfo.value!![it].name)
                    putString("path", viewModel.directoryInfo.value!![it].path)
                }
                findNavController().navigate(R.id.action_fragment_directory_to_fragment_internal_file, bundle)
            }
        }


        return view
    }
}