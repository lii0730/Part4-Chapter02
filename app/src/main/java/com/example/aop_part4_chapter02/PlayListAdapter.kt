package com.example.aop_part4_chapter02

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.aop_part4_chapter02.service.PlayListItem

class PlayListAdapter(val onItemClicked: (MusicModel) -> Unit) :
    ListAdapter<MusicModel, PlayListAdapter.ViewHolder>(diffUtil) {

    private var tmpView: View? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(item: MusicModel) {
            val titleTextView = itemView.findViewById<TextView>(R.id.titleTextView)
            val artistTextView = itemView.findViewById<TextView>(R.id.artistTextView)
            val thumbnailImageView: ImageView = itemView.findViewById(R.id.thumbnailImageView)

            titleTextView.text = item.track
            artistTextView.text = item.artist
            Glide.with(thumbnailImageView.context)
                .load(Uri.parse(item.cover))
                .into(thumbnailImageView)


            itemView.setOnClickListener {
                if (tmpView == null) {
                    itemView.setBackgroundColor(Color.GRAY)
                    tmpView = itemView
                } else {
                    tmpView?.let {
                        if (!it.equals(itemView)) {
                            itemView.setBackgroundColor(Color.GRAY)
                            it.setBackgroundColor(Color.TRANSPARENT)
                            tmpView = itemView
                        } else return@let
                    }
                }
//                item.isPlaying = !item.isPlaying
//                if(item.isPlaying) {
//                    itemView.setBackgroundColor(Color.GRAY)
//                } else {
//                    itemView.setBackgroundColor(Color.TRANSPARENT)
//                }
                onItemClicked(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<MusicModel>() {
            override fun areItemsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MusicModel, newItem: MusicModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}