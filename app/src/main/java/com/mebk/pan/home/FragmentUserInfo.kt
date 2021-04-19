package com.mebk.pan.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.utils.ToolUtils
import com.mebk.pan.vm.UserInfoViewModel
import de.hdodenhof.circleimageview.CircleImageView
import java.util.concurrent.atomic.AtomicReference

class FragmentUserInfo : Fragment() {
    private val viewModel by viewModels<UserInfoViewModel>()
    private lateinit var circleImageView: CircleImageView
    private lateinit var nicknameTv: TextView
    private lateinit var accountTv: TextView
    private lateinit var groupTv: TextView
    private lateinit var detailedInfoTv: TextView
    private lateinit var uploadHistory: LinearLayout
    private lateinit var downloadHistory: LinearLayout
    private lateinit var shareHistory: LinearLayout
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_user_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView(view)

        viewModel.userInfoData.observe(viewLifecycleOwner, {
            Glide.with(requireActivity())
                    .load(ToolUtils.splitUrl("https://pan.mebk.org/api/v3/user/avatar/",it.id, "/s"))
                    .placeholder(R.drawable.mine)
                    .dontAnimate()
                    .into(circleImageView)
            nicknameTv.text=it.nickname
            accountTv.text=it.user_name
            groupTv.text=it.groupName
        })
    }

    private fun initView(view: View) {
        circleImageView = view.findViewById(R.id.fragment_user_info_img)
        nicknameTv = view.findViewById(R.id.fragment_user_info_nickname)
        accountTv = view.findViewById(R.id.fragment_user_info_account)
        groupTv = view.findViewById(R.id.fragment_user_info_group)
        detailedInfoTv = view.findViewById(R.id.fragment_user_info_detailed_info)
        uploadHistory = view.findViewById(R.id.fragment_user_info_upload)
        downloadHistory = view.findViewById(R.id.fragment_user_info_download)
        shareHistory = view.findViewById(R.id.fragment_user_info_share)
    }
}