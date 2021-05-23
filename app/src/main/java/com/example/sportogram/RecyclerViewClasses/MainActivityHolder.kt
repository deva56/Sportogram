package com.example.sportogram.RecyclerViewClasses

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sportogram.Models.Post
import com.example.sportogram.R

class MainActivityHolder(
    itemView: View,
    val context: Context,
    val postAuthor: TextView = itemView.findViewById(R.id.PostAuthor),
    val postCreatedBefore: TextView = itemView.findViewById(R.id.PostCreatedBefore),
    val postViews: TextView = itemView.findViewById(R.id.PostViews),
    val postDescription: TextView = itemView.findViewById(R.id.PostDescription),
    val postThumbnail: ImageView = itemView.findViewById(R.id.PostThumbnail),
    val postProgressBar: ProgressBar = itemView.findViewById(R.id.PostProgressBar),
    val postVolumeControl: ImageView = itemView.findViewById(R.id.PostVolumeControl),
    val parent: View = itemView
) : RecyclerView.ViewHolder(itemView) {

    fun onBind(post: Post) {
        parent.tag = this
        postAuthor.text = post.author.name
        postCreatedBefore.text = post.createdBefore
        postViews.text = post.views
        postDescription.text = post.description
        Glide.with(context).load(post.video.poster).centerCrop()
            .into(postThumbnail)
    }

}