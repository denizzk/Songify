package com.dkarakaya.songify;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongHolder> {

    private ArrayList<SongInfo> _songs;
    private Context context;
    private OnItemClickListener mOnItemClickListener;

    public SongAdapter(Context context, ArrayList<SongInfo> songs) {
        this.context = context;
        this._songs = songs;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, SongInfo obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }


    @Override
    public SongHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View myView = LayoutInflater.from(context).inflate(R.layout.row_song,viewGroup,false);
        return new SongHolder(myView);
    }

    @Override
    public void onBindViewHolder(final SongHolder songHolder, final int i) {
        final SongInfo s = _songs.get(i);
        songHolder.tvSongName.setText(_songs.get(i).getSongTitle());
        songHolder.tvSongArtist.setText(_songs.get(i).getArtistName());
        songHolder.tvSongDuration.setText(_songs.get(i).getSongDuration());
        songHolder.cvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(v, s, i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return _songs.size();
    }

    public class SongHolder extends RecyclerView.ViewHolder {
        TextView tvSongName,tvSongArtist, tvSongDuration;
        CardView cvPlay;
        public SongHolder(View itemView) {
            super(itemView);
            tvSongName = itemView.findViewById(R.id.tvSongName);
            tvSongArtist = itemView.findViewById(R.id.tvArtistName);
            tvSongDuration = itemView.findViewById(R.id.tvSongDuration);
            cvPlay=itemView.findViewById(R.id.cvPlay);
        }
    }
}