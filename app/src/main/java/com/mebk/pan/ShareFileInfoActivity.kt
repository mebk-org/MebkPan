package com.mebk.pan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.chooseDirectoryThumbnail
import kotlinx.android.synthetic.main.activity_share_file_info.*

class ShareFileInfoActivity : AppCompatActivity() {
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
        shareFileInfo_share_download_tv.text = file.downloads.toString()
        shareFileInfo_share_limit_download_tv.text = file.remain_downloads.toString()
        shareFileInfo_share_limit_expire_tv.text = file.expire.toString()
        shareFileInfo_share_time_tv.text = file.create_date.toString()
        shareFileInfo_share_view_tv.text = file.views.toString()

    }
}