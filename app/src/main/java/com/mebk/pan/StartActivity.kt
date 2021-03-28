package com.mebk.pan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.SharePreferenceUtils
import com.mebk.pan.view.TimeBtn
import com.mebk.pan.view.WaitingAnimationEndInterface
import com.mebk.pan.vm.StartViewModel

class StartActivity : AppCompatActivity(), WaitingAnimationEndInterface {
    private lateinit var timeBtn: TimeBtn
    private val startViewModel by viewModels<StartViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        timeBtn = findViewById(R.id.start_activity_time_btn)
        timeBtn.startAnimation()
        startViewModel.getCookie(SharePreferenceUtils.getSharePreference(applicationContext).getString(SharePreferenceUtils.SP_KEY_UID, "")!!)

    }

    override fun waitingEnd(isEnd: Boolean) {
        if (MyApplication.isLogin) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}