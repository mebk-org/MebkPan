//package com.mebk.pan.home
//
//import android.os.Bundle
//import android.text.TextUtils
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.activity.addCallback
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Observer
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
//import com.mebk.pan.R
//import com.mebk.pan.aa.DirectoryRvAdapter
//import com.mebk.pan.database.entity.File
//import com.mebk.pan.dtos.DirectoryDto
//import com.mebk.pan.utils.LogUtil
//import com.mebk.pan.vm.InternalFileViewModel
//
//class FragmentInternalFile : Fragment() {
//    private lateinit var rv: RecyclerView
//    private lateinit var sr: SwipeRefreshLayout
//    private val viewModel by viewModels<InternalFileViewModel>()
//    private var path: String? = null
//    private var name: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            path = it.getString("path")
//            name = it.getString("name")
//        }
//    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = layoutInflater.inflate(R.layout.fragment_internal_file, null)
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        rv = view.findViewById(R.id.fragment_internal_file_rv)
//        sr = view.findViewById(R.id.fragment_internal_file_sr)
//        sr.setProgressViewEndTarget(true, 300)
//        val list: MutableList<File> = mutableListOf()
//
//
//
//        if (TextUtils.isEmpty(name) || (TextUtils.isEmpty(path))) {
//            Toast.makeText(activity, "打开失败，请尝试刷新后重新获取", Toast.LENGTH_SHORT).show()
//            findNavController().navigate(R.id.action_fragment_internal_file_to_fragment_directory)
//        } else {
//            sr.isRefreshing = true
//            viewModel.internalFile(name!!)
//        }
//
//
//        val adapter = context?.let { DirectoryRvAdapter(it, list) }
//        rv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        rv.adapter = adapter
//
//        adapter!!.setOnClickListener {
//            viewModel.internalFile(viewModel.fileInfo.value!![it].name, viewModel.fileInfo.value!![it].path)
//        }
//
//        viewModel.fileInfo.observe(viewLifecycleOwner, Observer {
//            LogUtil.err(this.javaClass, "observe")
//            list.clear()
//            list.addAll(it)
//            adapter.notifyDataSetChanged()
//            sr.isRefreshing = false
//        })
//
//        //拦截返回事件
//        val callBack = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
//
//            if (!viewModel.back()) {
//                //todo
//                LogUtil.err(this.javaClass, "拦截")
//            } else {
//                LogUtil.err(this.javaClass, "不拦截")
//            }
//        }
//
//        viewModel.stackSize.observe(viewLifecycleOwner, Observer {
//            LogUtil.err(this.javaClass, "stack size=$it")
//            callBack.isEnabled = it > 1
//        })
//
//    }
//
//}