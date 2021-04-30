package com.mebk.pan

import android.app.ActionBar
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.*
import kotlinx.android.synthetic.main.activity_share_file_info.*

class ShareFileInfoActivity : AppCompatActivity() {
    var pwd = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_file_info)

        val bundle = intent.getBundleExtra("fileInfo")
        val file = bundle?.getParcelable<ShareHistoryEntity>("file")
        if (file == null) {
            Toast.makeText(this, "参数传递错误，请联系管理员", Toast.LENGTH_SHORT).show()
            finish()
        }
        val type = if (file!!.is_dir) "dir" else "file"

        Glide.with(this).load(ContextCompat.getDrawable(this, chooseDirectoryThumbnail(type, file.name))).into(shareFileInfo_iv)
        shareFileInfo_name.text = file.name
        shareFileInfo_share_download_tv.text = "${file.downloads} 次"
        shareFileInfo_share_limit_download_tv.text = if (file.remain_downloads == -1) "无限制" else "${file.remain_downloads} 次"
        shareFileInfo_share_limit_expire_tv.text = if (file.expire == -1L) "无限制" else second2Time(file.expire)
        shareFileInfo_share_time_tv.text = timeStamp2String(file.create_date)
        shareFileInfo_share_view_tv.text = "${file.views} 次"
        if (TextUtils.isEmpty(file.password)) {
            Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.unlock_24)).into(shareFileInfo_pwd_iv)
        }

        if (file.preview) {
            Glide.with(this).load(ContextCompat.getDrawable(this, R.drawable.share_preview_24)).into(shareFileInfo_preview)
        }


        shareFileInfo_share_copyClient_btn.setOnClickListener {
            val snackBar = Snackbar.make(shareFileInfo_coordinator, "分享成功", 10000)
            LogUtil.err(this@ShareFileInfoActivity.javaClass, "key=${file.key}")
            LogUtil.err(this@ShareFileInfoActivity.javaClass, "pwd=${file.password}")
            with(snackBar) {
                setAction(resources.getString(R.string.copy)) { v: View? ->
                    var res = BASE_URL + "s/" + file.key
                    if (!TextUtils.isEmpty(file.password)) {
                        res += " 提取密码: "
                        res += file.password
                    }
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("share client", res)
                    LogUtil.err(this@ShareFileInfoActivity.javaClass, "client=$res")
                    clipboard.setPrimaryClip(clipData)
                }
                show()
            }
        }



    }
}