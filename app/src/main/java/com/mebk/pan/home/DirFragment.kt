package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mebk.pan.R
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.vm.DirViewModel

class DirFragment : Fragment() {
    private val viewModel by viewModels<DirViewModel>()
    private var dirList = mutableListOf<String>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_dir, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getDir("/")


        viewModel.dirInfo.observe(viewLifecycleOwner, {
            LogUtil.err(this.javaClass, "dir=$it")
        })
    }
}