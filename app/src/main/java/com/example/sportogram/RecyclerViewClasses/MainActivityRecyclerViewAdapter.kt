package com.example.sportogram.RecyclerViewClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.sportogram.Models.Post
import com.example.sportogram.R

class MainActivityRecyclerViewAdapter(
    private var records: List<Post>,
    private val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainActivityHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.post_item, parent, false)
        return MainActivityHolder(itemView, context)
    }

    fun setRecords(records: List<Post>) {
        this.records = records
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return records.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentRecord = records[position]
        (holder as MainActivityHolder).onBind(currentRecord)
    }
}