package com.mebk.pan.aa

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.LogUtil
import com.mebk.pan.utils.ToolUtils

class DirectoryRvAdapter(val context: Context, val list: List<DirectoryDto.Object>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var clickListener: ((Int) -> Unit)


    private fun setOnClickListener(clickListener: ((Int) -> Unit)) {
        this.clickListener = clickListener
    }

    class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val thumbnailIv: ImageView
        internal val chooseIv: ImageView
        internal val filenameTv: TextView
        internal val timeTv: TextView
        internal val sizeTv: TextView

        init {
            thumbnailIv = itemView.findViewById(R.id.rv_item_directory_thumbnail_iv)
            chooseIv = itemView.findViewById(R.id.rv_item_directory_choose_iv)
            filenameTv = itemView.findViewById(R.id.rv_item_directory_filename_tv)
            timeTv = itemView.findViewById(R.id.rv_item_directory_time_tv)
            sizeTv = itemView.findViewById(R.id.rv_item_directory_size_tv)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_directory, parent, false)
        view.setOnClickListener {
            clickListener(view.tag as Int)
        }
        return DirectoryViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var vh = holder as DirectoryViewHolder
        LogUtil.err(this.javaClass, list[position].name)
        vh.filenameTv.text = list[position].name
        Glide.with(context).load(chooseDirectoryThumbnail(list[position].type, list[position].name)).into(vh.thumbnailIv)
        vh.timeTv.text = list[position].date
        vh.sizeTv.text = ToolUtils.sizeChange(list[position].size)

        vh.itemView.tag = list[position]
    }

    //根据文件类型与文件名选择缩略图
    private fun chooseDirectoryThumbnail(type: String, name: String): Drawable {
        return when {
            type == "dir" -> context.resources.getDrawable(R.drawable.directory_32, null)
            name.endsWith(".zip") || name.endsWith(".rar") || name.endsWith(".7z") -> context.resources.getDrawable(R.drawable.file_zip, null)
            name.endsWith(".txt") -> context.resources.getDrawable(R.drawable.file_txt, null)
            name.endsWith(".doc") || name.endsWith(".docx") -> context.resources.getDrawable(R.drawable.file_word, null)
            name.endsWith(".xls") || name.endsWith(".xlsx") -> context.resources.getDrawable(R.drawable.file_excel, null)
            name.endsWith(".pdf") -> context.resources.getDrawable(R.drawable.file_pdf, null)
            name.endsWith(".m4a") || name.endsWith(".mp3") || name.endsWith(".aac") || name.endsWith(".wma") -> context.resources.getDrawable(R.drawable.file_music, null)
            name.endsWith(".mp4") || name.endsWith(".flv") || name.endsWith(".avi") || name.endsWith(".wmp") -> context.resources.getDrawable(R.drawable.file_play, null)
            name.endsWith(".bin") -> context.resources.getDrawable(R.drawable.file_binary, null)
            name.endsWith(".bat") || name.endsWith(".sh") -> context.resources.getDrawable(R.drawable.file_code, null)
            else -> context.resources.getDrawable(R.drawable.file_32, null)
        }
    }

}