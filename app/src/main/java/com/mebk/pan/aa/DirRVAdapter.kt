package com.mebk.pan.aa

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mebk.pan.R
import com.mebk.pan.database.entity.File
import kotlinx.android.synthetic.main.rv_item_dir_table.*

class DirRVAdapter(val context: Context, val list: List<File>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var clickListener: ((Int) -> Unit)
    fun setClickListener(clickListener: ((Int) -> Unit)) {
        this.clickListener = clickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_dir, parent, false)
        view.setOnClickListener {
            clickListener(view.tag as Int)
        }
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder as MyViewHolder) {
            name.text = list[position].name
            itemView.tag = position
        }

    }

    override fun getItemCount() = list.size

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.rv_item_dir_tv)
    }
}