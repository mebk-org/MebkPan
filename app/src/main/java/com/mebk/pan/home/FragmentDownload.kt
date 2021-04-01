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
import com.mebk.pan.R
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DownloadViewModel


class FragmentDownload : Fragment() {
    companion object {
        private const val ARG_PATH = "path"
        private const val ARG_ID = "id"
    }

    private var path: String? = null
    private var id: String? = null
    private val downloadViewModel by viewModels<DownloadViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            path = it.getString(ARG_PATH)
            id = it.getString(ARG_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_download, container, false)
        LogUtil.err(this.javaClass, "path=$path,id=$id")
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(id)) {
            Toast.makeText(activity, "无法获取参数，请重新打开", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_downloadFragment_to_fragment_directory)
        } else {
            downloadViewModel.download(id!!)
        }

        downloadViewModel.downloadClientInfo.observe(viewLifecycleOwner, Observer {
            Toast.makeText(activity, it, Toast.LENGTH_SHORT).show()
        })


        return v
    }

}