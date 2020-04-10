package com.dkarakaya.songify.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dkarakaya.songify.R
import com.dkarakaya.songify.adapter.VideoPostAdapter.YoutubePostHolder
import com.dkarakaya.songify.model.YoutubeDataModel
import com.dkarakaya.songify.util.OnItemClickListener
import com.squareup.picasso.Picasso
import java.util.*

class VideoPostAdapter internal constructor(private val dataSet: ArrayList<YoutubeDataModel>, private val listener: OnItemClickListener) : RecyclerView.Adapter<YoutubePostHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YoutubePostHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.youtube_post, parent, false)
        return YoutubePostHolder(view)
    }

    override fun onBindViewHolder(holder: YoutubePostHolder, position: Int) {
        //set the views here
        val textViewTitle = holder.textViewTitle
        val textViewChannelTitle = holder.textViewChannelTitle
        val textViewDate = holder.textViewDate
        val imageView = holder.imageView

        val (title, channelTitle, publishedAt, thumbnail) = dataSet[position]
        textViewTitle.text = title
        textViewChannelTitle.text = channelTitle
        textViewDate.text = publishedAt
        holder.bind(dataSet[position], listener)

        //TODO: image will be downloaded from url
        Picasso.get()
                .load(thumbnail)
                .into(imageView)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    class YoutubePostHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        var textViewChannelTitle: TextView = itemView.findViewById(R.id.textViewChannelTitle)
        var textViewDate: TextView = itemView.findViewById(R.id.textViewDate)
        var imageView: ImageView = itemView.findViewById(R.id.ImageThumb)

        fun bind(item: YoutubeDataModel, listener: OnItemClickListener) {
            itemView.setOnClickListener { listener.onItemClick(item) }
        }
    }

}
