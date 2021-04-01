package com.mebk.pan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.viewModels
import com.mebk.pan.application.MyApplication
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.SharePreferenceUtils
import com.mebk.pan.view.TimeBtn
import com.mebk.pan.view.WaitingAnimationEndInterface
import com.mebk.pan.vm.StartViewModel

class StartActivity : AppCompatActivity(), WaitingAnimationEndInterface {
    private var isValid: Boolean = false
    private lateinit var timeBtn: TimeBtn
    private val startViewModel by viewModels<StartViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        timeBtn = findViewById(R.id.start_activity_time_btn)
        timeBtn.startAnimation()
        if (MyApplication.isLogin) {
            startViewModel.getCookie(SharePreferenceUtils.getSharePreference(applicationContext).getString(SharePreferenceUtils.SP_KEY_UID, "")!!)
        }
        LogUtil.err(this.javaClass, "剩余时间=${(SystemClock.elapsedRealtime() -
                SharePreferenceUtils.getSharePreference(this).getLong(SharePreferenceUtils.SP_KEY_LOGIN_TIME, 0)) / 1000}")

        if (MyApplication.isLogin) {
            isValid = (SystemClock.elapsedRealtime() -
                    SharePreferenceUtils.getSharePreference(this).getLong(SharePreferenceUtils.SP_KEY_LOGIN_TIME, 0)) / 1000 < (SharePreferenceUtils.getSharePreference(this).getLong(SharePreferenceUtils.SP_KEY_COOKIE_VALID, 0)) - 3600

            if (!isValid) {
                Toast.makeText(this, "登录已过期，请重新登录", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun waitingEnd(isEnd: Boolean) {
        if (MyApplication.isLogin && isValid) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}