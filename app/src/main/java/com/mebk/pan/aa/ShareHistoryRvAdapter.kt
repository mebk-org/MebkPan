package com.mebk.pan.aa

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.database.entity.ShareHistoryEntity
import com.mebk.pan.utils.DATE_TYPE_UTC
import com.mebk.pan.utils.chooseDirectoryThumbnail
import com.mebk.pan.utils.timeStamp2String
import com.mebk.pan.utils.utcToLocal

class ShareHistoryRvAdapter(val context: Context, val list: List<ShareHistoryEntity>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var clickListener: ((Int) -> Unit)
    fun setClickListener(clickListener: ((Int) -> Unit)) {
        this.clickListener = clickListener
    }

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv = itemView.findViewById<ImageView>(R.id.rv_item_share_history_iv)
        val nameTv = itemView.findViewById<TextView>(R.id.rv_item_share_history_name_tv)
        val timeTv = itemView.findViewById<TextView>(R.id.rv_item_share_history_time_tv)
        val downloadBnNumTv = itemView.findViewById<TextView>(R.id.rv_item_share_history_download_tv)
        val previewNumTv = itemView.findViewById<TextView>(R.id.rv_item_share_history_preview_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_share_history, parent, false)
        view.setOnClickListener {
            clickListener(view.tag as Int)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder as MyViewHolder) {
            val type = if (list[position].is_dir) "dir" else "file"
            Glide.with(context).load(ContextCompat.getDrawable(context, chooseDirectoryThumbnail(type, list[position].name))).into(iv)
            nameTv.text = list[position].name
            downloadBnNumTv.text = "下载次数： ${list[position].downloads}"
            previewNumTv.text = "游览次数： ${list[position].views}"
            timeTv.text = timeStamp2String(list[position].create_date)
            itemView.tag = position
        }
    }

    override fun getItemCount() = list.size
}