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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.rv_item_dir, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).name.text = list[position].name
    }

    override fun getItemCount() = list.size

    private class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.rv_item_dir_tv)
    }
}