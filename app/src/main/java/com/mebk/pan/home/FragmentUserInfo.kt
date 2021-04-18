package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mebk.pan.R
import java.util.concurrent.atomic.AtomicReference

class FragmentUserInfo : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_info, container, false)
        return view
    }
}