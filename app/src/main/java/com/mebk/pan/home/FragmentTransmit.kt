package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mebk.pan.R
import com.mebk.pan.aa.TransmitVpAdapter
import com.mebk.pan.utils.LogUtil

class FragmentTransmit : Fragment() {
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private val tabs = arrayOf("文件上传", "文件下载")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transmit, container, false)

        tabLayout = view.findViewById(R.id.fragment_transmit_tabLayout)

        viewPager = view.findViewById(R.id.fragment_transmit_vp)
        viewPager.adapter = TransmitVpAdapter(this)


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            LogUtil.err(this.javaClass, "pos=${position}")
            tab.text = tabs[position]
        }.attach()


        return view
    }

}