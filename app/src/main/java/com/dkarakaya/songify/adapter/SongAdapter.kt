package com.dkarakaya.songify.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dkarakaya.songify.R
import com.dkarakaya.songify.adapter.SongAdapter.SongHolder
import com.dkarakaya.songify.model.SongInfo

class SongAdapter(private val context: Context, private val _songs: MutableList<SongInfo>) : RecyclerView.Adapter<SongHolder>() {
    private var mOnItemClickListener: OnItemClickListener? = null

    interface OnItemClickListener {
        fun onItemClick(view: View?, obj: SongInfo?, position: Int)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener) {
        mOnItemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SongHolder {
        val myView = LayoutInflater.from(context).inflate(R.layout.row_song, viewGroup, false)
        return SongHolder(myView)
    }

    override fun onBindViewHolder(songHolder: SongHolder, i: Int) {
        val s = _songs[i]
        songHolder.tvSongName.text = _songs[i].songTitle
        songHolder.tvSongArtist.text = _songs[i].artistName
        songHolder.tvSongDuration.text = _songs[i].songDuration
        songHolder.cvPlay.setOnClickListener { v ->
            if (mOnItemClickListener != null) {
                mOnItemClickListener!!.onItemClick(v, s, i)
            }
        }
    }

    override fun getItemCount(): Int {
        return _songs.size
    }

    inner class SongHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvSongName: TextView = itemView.findViewById(R.id.tvSongName)
        var tvSongArtist: TextView = itemView.findViewById(R.id.tvArtistName)
        var tvSongDuration: TextView = itemView.findViewById(R.id.tvSongDuration)
        var cvPlay: CardView = itemView.findViewById(R.id.cvPlay)

    }

}
