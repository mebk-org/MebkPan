package com.mebk.pan.aa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.database.entity.DownloadingInfo
import com.mebk.pan.utils.RetrofitClient
import com.mebk.pan.utils.ToolUtils

class DownloadRvAdapter(val list: List<DownloadingInfo>, val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val TAG = 0
    private val FILE = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TAG -> TagViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item_history_download_tag, parent, false))
            else -> WaitingViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_item_downloading, parent, false))
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is WaitingViewHolder -> {
                with(holder) {
                    Glide.with(context).load(ToolUtils.chooseDirectoryThumbnail(list[position].type, list[position].name)).into(thumbnailIv)
                    filenameTv.text = list[position].name
                    holder.itemView.tag = position
                    stateTv.text = RetrofitClient.checkDownloadState(list[position].state!!)
                    if (list[position].state == RetrofitClient.DOWNLOAD_STATE_DOWNLOADING) {
                        progressBar.visibility = View.VISIBLE
                        sizeTv.visibility = View.VISIBLE
                        sizeTv.text = "0 Mb/s"
                    } else {
                        sizeTv.visibility = View.INVISIBLE
                        progressBar.visibility = View.INVISIBLE
                    }
                }
            }
            is TagViewHolder -> {
                holder.tagTv.text = list[position].name
            }
        }

    }

    private class WaitingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val thumbnailIv: ImageView = itemView.findViewById(R.id.rv_item_history_download_waiting_thumbnail_iv)
        internal val check: CheckBox = itemView.findViewById(R.id.rv_item_history_download_waiting_choose)
        internal val filenameTv: TextView = itemView.findViewById(R.id.rv_item_history_download_waiting_filename_tv)
        internal val sizeTv: TextView = itemView.findViewById(R.id.rv_item_history_download_waiting_size_tv)
        internal val stateTv: TextView = itemView.findViewById(R.id.rv_item_history_download_waiting_size_state)
        internal val moreIv: ImageView = itemView.findViewById(R.id.rv_item_history_download_waiting_more_iv)
        internal val progressBar: ProgressBar = itemView.findViewById(R.id.rv_item_history_download_waiting_progress)
    }

    private class TagViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val tagTv: TextView = itemView.findViewById(R.id.rv_item_history_download_done_tag)
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].type) {
            "tag" -> TAG
            else -> FILE
        }
    }
}