package com.mebk.pan.aa

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mebk.pan.R
import com.mebk.pan.database.entity.File
import com.mebk.pan.dtos.DirectoryDto
import com.mebk.pan.utils.*

class DirectoryRvAdapter(private val context: Context, val list: List<File>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var clickListener: ((Int) -> Unit)
    private lateinit var clickOnLongListener: ((Int) -> Unit)
    var isFileOperator = false
    private lateinit var clickMoreImageViewListener: ((Int) -> Unit)
    private lateinit var clickCheckBoxListener: ((Int, Boolean) -> Unit)

    companion object {
        //文件
        private const val TYPE_DIRECTORY = 0

        //刷新
        private const val TYPE_REFRESH = 1
    }

    fun setOnClickListener(clickListener: ((Int) -> Unit)) {
        this.clickListener = clickListener
    }

    fun setOnLongClickListener(clickOnLongListener: (Int) -> Unit) {
        this.clickOnLongListener = clickOnLongListener
    }

    fun setOnClickMoreImageViewListener(clickMoreImageViewListener: ((Int) -> Unit)) {
        this.clickMoreImageViewListener = clickMoreImageViewListener
    }

    fun setOnClickCheckBoxListener(clickCheckBoxListener: ((Int, Boolean) -> Unit)) {
        this.clickCheckBoxListener = clickCheckBoxListener
    }

    class DirectoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val thumbnailIv: ImageView = itemView.findViewById(R.id.rv_item_directory_thumbnail_iv)
        internal val check: CheckBox = itemView.findViewById(R.id.rv_item_directory_choose)
        internal val filenameTv: TextView = itemView.findViewById(R.id.rv_item_directory_filename_tv)
        internal val timeTv: TextView = itemView.findViewById(R.id.rv_item_directory_time_tv)
        internal val sizeTv: TextView = itemView.findViewById(R.id.rv_item_directory_size_tv)
        internal val moreIv: ImageView = itemView.findViewById(R.id.rv_item_directory_more_iv)
    }

    class RefreshViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val refreshMsgTv: TextView = itemView.findViewById(R.id.rv_item_directory_refresh_msg_tv)
        internal val lastTimeTv: TextView = itemView.findViewById(R.id.rv_item_directory_lastTime_tv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DIRECTORY -> {
                val view = LayoutInflater.from(context).inflate(R.layout.rv_item_directory, parent, false)
                view.setOnClickListener {
                    if (isFileOperator) {
                        val checkBox = view.findViewById<CheckBox>(R.id.rv_item_directory_choose)
                        checkBox.isChecked = !checkBox.isChecked
                    } else {
                        clickListener(view.tag as Int)
                    }
                }

                view.setOnLongClickListener {
                    clickOnLongListener(view.tag as Int)
                    true
                }

                DirectoryViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(context).inflate(R.layout.rv_item_directory_refresh, parent, false)
                RefreshViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is DirectoryViewHolder -> {

                holder.filenameTv.text = list[position].name
                Glide.with(context).load(ContextCompat.getDrawable(context, chooseDirectoryThumbnail(list[position].type, list[position].name))).into(holder.thumbnailIv)
                holder.timeTv.text = timeStamp2String(list[position].date)
                holder.itemView.tag = position

                if ("dir" != list[position].type) {
                    holder.sizeTv.text = sizeChange(list[position].size)
                } else {
                    holder.sizeTv.visibility = GONE
                }

                holder.check.visibility = if (isFileOperator) VISIBLE else INVISIBLE
                holder.moreIv.visibility = if (!isFileOperator) VISIBLE else INVISIBLE
                holder.check.isChecked = false
                holder.moreIv.setOnClickListener {
                    clickMoreImageViewListener(position)
                }

                holder.check.setOnCheckedChangeListener { buttonView, isChecked ->
                    clickCheckBoxListener(position, isChecked)
                }

            }
            is RefreshViewHolder -> {
                holder.refreshMsgTv.text = list[position].name
                holder.lastTimeTv.text = timeStamp2String(list[position].date)
            }
            else -> {
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].type) {
            "refresh" -> TYPE_REFRESH
            else -> TYPE_DIRECTORY
        }

    }


}