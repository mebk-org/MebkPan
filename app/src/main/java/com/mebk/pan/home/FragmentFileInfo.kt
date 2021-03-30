package com.mebk.pan.home

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.mebk.pan.R
import com.mebk.pan.utils.ToolUtils
import com.mebk.pan.vm.FileInfoViewModel

class FragmentFileInfo : Fragment() {
    private val fileInfoViewModel by viewModels<FileInfoViewModel>()
    private lateinit var downloadBtn: Button
    private lateinit var sizeTv: TextView
    private lateinit var policyTv: TextView
    private lateinit var pathTv: TextView
    private lateinit var createTimeTv: TextView
    private lateinit var alterTimeTv: TextView
    private lateinit var nameTv: TextView

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_TYPE = "type"
        private const val ARG_NAME = "name"
    }

    private var id: String? = null
    private var type: String? = null
    private var name: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getString(ARG_ID)
            type = it.getString(ARG_TYPE)
            name = it.getString(ARG_NAME)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_file_info, container, false)
        if (id == null || type == null || name == null) {
            findNavController().navigate(R.id.action_fragmentFileInfo_to_fragment_directory)
        }


        downloadBtn = view.findViewById(R.id.fragment_file_info_download_btn)
        sizeTv = view.findViewById(R.id.fragment_file_info_size)
        policyTv = view.findViewById(R.id.fragment_file_info_policy)
        pathTv = view.findViewById(R.id.fragment_file_info_dir)
        createTimeTv = view.findViewById(R.id.fragment_file_info_createTime)
        alterTimeTv = view.findViewById(R.id.fragment_file_info_alterTime)
        nameTv = view.findViewById(R.id.fragment_file_info_name)

        nameTv.text = name

        downloadBtn.isClickable = false
        downloadBtn.setOnClickListener {
            val path = activity?.getExternalFilesDir(null)!!.path + name
            fileInfoViewModel.writeFile(path)
            with(downloadBtn) {
                setBackgroundColor(ResourcesCompat.getColor(resources, R.color.communism_clink, null))
                isClickable = false
                text = "下载中..."
                setTextColor(ResourcesCompat.getColor(resources, R.color.black, null))
            }
        }



        fileInfoViewModel.getDownloadClient(id!!)

        fileInfoViewModel.getFileInfo(id!!, type!!)

        fileInfoViewModel.downloadClientInfo.observe(viewLifecycleOwner, Observer {
            with(downloadBtn) {
                setBackgroundColor(ResourcesCompat.getColor(resources, R.color.communism, null))
                isClickable = true
                text = "下载"
                setTextColor(ResourcesCompat.getColor(resources, R.color.white, null))
            }
        })


        fileInfoViewModel.fileInfo.observe(viewLifecycleOwner, Observer {
            sizeTv.text = ToolUtils.sizeChange(it.size)
            policyTv.text = it.policy
            pathTv.text = if (TextUtils.isEmpty(it.path)) "根目录" else it.path
            createTimeTv.text = it.created_at
            alterTimeTv.text = it.updated_at
        })

        return view
    }
}