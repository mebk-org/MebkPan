package com.mebk.pan

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mebk.pan.utils.ToolUtils
import com.mebk.pan.vm.UserInfoViewModel
import de.hdodenhof.circleimageview.CircleImageView

private const val TAG = "UserInfoActivity"
class UserInfoActivity : AppCompatActivity() {
    private val viewModel by viewModels<UserInfoViewModel>()
    private lateinit var circleImageView: CircleImageView
    private lateinit var nicknameTv: TextView
    private lateinit var accountTv: TextView
    private lateinit var groupTv: TextView
    private lateinit var detailedInfoTv: TextView
    private lateinit var uploadHistory: LinearLayout
    private lateinit var downloadHistory: LinearLayout
    private lateinit var shareHistory: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)
        Log.e(TAG, "onCreate: ", )
        initView()

        viewModel.userInfoData.observe(this, {
            Glide.with(this)
                    .load(ToolUtils.splitUrl("https://pan.mebk.org/api/v3/user/avatar/", it.id, "/s"))
                    .placeholder(R.drawable.mine)
                    .dontAnimate()
                    .into(circleImageView)
            nicknameTv.text = it.nickname
            accountTv.text = it.user_name
            groupTv.text = it.groupName
        })

        downloadHistory.setOnClickListener {
            startActivity(Intent(this, HistoryDownloadActivity::class.java))
        }

    }

    private fun initView() {
        circleImageView = findViewById(R.id.user_info_img)
        nicknameTv = findViewById(R.id.user_info_nickname)
        accountTv = findViewById(R.id.user_info_account)
        groupTv = findViewById(R.id.user_info_group)
        detailedInfoTv = findViewById(R.id.user_info_detailed_info)
        uploadHistory = findViewById(R.id.user_info_upload)
        downloadHistory = findViewById(R.id.user_info_download)
        shareHistory = findViewById(R.id.user_info_share)
    }
}