package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mebk.pan.R
import com.mebk.pan.aa.DirRVAdapter
import com.mebk.pan.database.entity.File
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DirViewModel

class DirFragment : Fragment() {
    private val viewModel by viewModels<DirViewModel>()
    private var dirList = mutableListOf<File>()
    private lateinit var adapter: DirRVAdapter
    private lateinit var rv: RecyclerView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dir, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getDir()
        rv = view.findViewById(R.id.fragment_dir_rv)
        rv.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        adapter = DirRVAdapter(requireContext(), dirList)

        rv.adapter = adapter

        adapter.setClickListener {
            viewModel.getDir(dirList[it].path, dirList[it].name)
        }


        viewModel.dirInfo.observe(viewLifecycleOwner, {
            dirList.clear()
            dirList.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }
}