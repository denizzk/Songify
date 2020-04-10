package com.dkarakaya.songify

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.dkarakaya.songify.SongAdapter.SongHolder

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
        var tvSongName: TextView
        var tvSongArtist: TextView
        var tvSongDuration: TextView
        var cvPlay: CardView

        init {
            tvSongName = itemView.findViewById(R.id.tvSongName)
            tvSongArtist = itemView.findViewById(R.id.tvArtistName)
            tvSongDuration = itemView.findViewById(R.id.tvSongDuration)
            cvPlay = itemView.findViewById(R.id.cvPlay)
        }
    }

}
