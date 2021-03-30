package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mebk.pan.R

class FragmentFileInfo : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_file_info, container,false)
        if (id == null) {
            findNavController().navigate(R.id.action_fragmentFileInfo_to_fragment_directory)
        }


        return view
    }
}