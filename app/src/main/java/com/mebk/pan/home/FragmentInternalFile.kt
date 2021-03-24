package com.mebk.pan.home

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.mebk.pan.R
import com.mebk.pan.aa.DirectoryRvAdapter
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.InternalFileViewModel

class FragmentInternalFile : Fragment() {
    private lateinit var rv: RecyclerView
    private lateinit var sr: SwipeRefreshLayout
    val viewModel by viewModels<InternalFileViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_internal_file, null)

        rv = view.findViewById(R.id.fragment_internal_file_rv)
        sr = view.findViewById(R.id.fragment_internal_file_sr)
        sr.setProgressViewEndTarget(true, 300)
        var list: MutableList<DirectoryDto.Object> = mutableListOf()

        val path = arguments?.getString("path", "")
        val name = arguments?.getString("name", "")

        if (TextUtils.isEmpty(name) || (TextUtils.isEmpty(path))) {
            Toast.makeText(activity, "打开失败，请尝试刷新后重新获取", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_fragment_internal_file_to_fragment_directory)
        } else {
            sr.isRefreshing = true
            viewModel.internalFile(name!!, path!!)
        }


        var adapter = context?.let { DirectoryRvAdapter(it, list) }
        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rv.adapter = adapter



        viewModel.flieInfo.observe(viewLifecycleOwner, Observer {
            LogUtil.err(this.javaClass,"observe")
            list.clear()
            list.addAll(it)
            adapter?.notifyDataSetChanged()
            sr.isRefreshing = false
        })


        return view
    }


}