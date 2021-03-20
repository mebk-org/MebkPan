package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mebk.pan.R
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DirectoryViewModel
import kotlinx.android.synthetic.main.fragment_directory.*

class FragmentDirectory : Fragment() {
    private lateinit var rv: RecyclerView
    private val viewModel by viewModels<DirectoryViewModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_directory, container, false)
        var list: MutableList<DirectoryDto.Object> = mutableListOf()
        rv = view.findViewById(R.id.fragment_directory_rv)
        viewModel.directory()


        var adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter

        viewModel.directoryInfo.observe(viewLifecycleOwner, Observer {
            list.clear()
            list.addAll(it)
            for (dto in list) {
                LogUtil.err(this.context!!::class.java, dto.toString())
            }
            adapter?.notifyDataSetChanged()
        })



        return view
    }
}