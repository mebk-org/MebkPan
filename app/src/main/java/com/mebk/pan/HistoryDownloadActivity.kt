package com.mebk.pan

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.mebk.pan.aa.HistoryDownloadRvAdapter
import com.mebk.pan.database.entity.DownloadingInfoEntity
import com.mebk.pan.utils.getFileMimeType
import com.mebk.pan.vm.HistoryDownloadViewModel
import java.io.File

class HistoryDownloadActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: HistoryDownloadRvAdapter
    private var list = mutableListOf<DownloadingInfoEntity>()
    private val viewModel by viewModels<HistoryDownloadViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_histroy_download)
        rv = findViewById(R.id.fragment_history_download_rv)
        rv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        (rv.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        adapter = HistoryDownloadRvAdapter(list, this)
        rv.adapter = adapter

        viewModel.downloadListInfo.observe(this, {
            list.clear()
            list.addAll(it)
            adapter.notifyDataSetChanged()
        })
        adapter.setOnClickListener {
            val ext = MimeTypeMap.getFileExtensionFromUrl(getFileMimeType(list[it].name))
            val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
            val path = (list[it].path).replaceFirst("/", "") + list[it].name
            val file = File(path)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    val contentUri = FileProvider.getUriForFile(this@HistoryDownloadActivity, "com.mebk.pan.fileProvider", file);
                    setDataAndType(contentUri, mimeType)
                } else {
                    intent.setDataAndType(Uri.fromFile(file), mimeType)
                }
            }

            startActivity(Intent.createChooser(intent, list[it].name))
        }
    }
}