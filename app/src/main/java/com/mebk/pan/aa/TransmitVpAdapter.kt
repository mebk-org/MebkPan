package com.mebk.pan.aa

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mebk.pan.home.FragmentDownloading
import com.mebk.pan.home.FragmentHistoryDownload
import com.mebk.pan.home.FragmentHistoryUplaod

class TransmitVpAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> {
                return FragmentHistoryUplaod()
            }
            1 -> {
                return FragmentDownloading()
            }

        }
        return FragmentHistoryUplaod()
    }
}