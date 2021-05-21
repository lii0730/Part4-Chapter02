package com.example.aop_part4_chapter02

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlayListAdapter : ListAdapter<PlayListItem, PlayListAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(itemView : View) :RecyclerView.ViewHolder(itemView){
        fun bind(item : PlayListItem) {
            val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
            val artistTextView = itemView.findViewById<TextView>(R.id.artistTextView)
            val thumbnailImageView : ImageView = itemView.findViewById(R.id.thumbnailImageView)

            titleTextView.text = item.title
            artistTextView.text = item.artist
            Glide.with(thumbnailImageView.context)
                .load(item.cover)
                .into(thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<PlayListItem>() {
            override fun areItemsTheSame(oldItem: PlayListItem, newItem: PlayListItem): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: PlayListItem, newItem: PlayListItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}