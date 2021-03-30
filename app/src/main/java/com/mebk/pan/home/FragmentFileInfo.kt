package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mebk.pan.R
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.FileInfoViewModel

class FragmentFileInfo : Fragment() {
    private val fileInfoViewModel by viewModels<FileInfoViewModel>()

    companion object {
        private const val ARG_ID = "id"
    }

    private var id: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_file_info, container, false)
        if (id == null) {
            findNavController().navigate(R.id.action_fragmentFileInfo_to_fragment_directory)
        }

        fileInfoViewModel.download(id!!)

        fileInfoViewModel.downloadClientInfo.observe(viewLifecycleOwner, Observer {
            LogUtil.err(this.javaClass, it)
        })





        return view
    }
}