package com.dkarakaya.songify.library

import android.annotation.SuppressLint
import android.content.Context
import android.provider.MediaStore
import com.dkarakaya.songify.adapter.SongAdapter
import com.dkarakaya.songify.model.SongInfo
import com.dkarakaya.songify.util.formatDuration
import com.dkarakaya.songify.util.formatTitle

class SongListLoader {
    @SuppressLint("InlinedApi")
    operator fun invoke(context: Context, songList: MutableList<SongInfo>): SongAdapter {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val audioType = "${MediaStore.Audio.Media.MIME_TYPE} == 'audio/mp4' OR ${MediaStore.Audio.Media.MIME_TYPE} == 'audio/mpeg'"
        val selection = "${MediaStore.Audio.Media.IS_MUSIC}!=0 AND $audioType AND ${MediaStore.Audio.Media.DATA} LIKE '%music%'"
        val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} ASC"
        val cursor = context.contentResolver.query(uri, null, selection, null, sortOrder)
        cursor?.use {
            while (cursor.moveToNext()) {
                var title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                var artist: String? = null
                val url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                val duration = formatDuration(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)))

                val array = title.split("-").toTypedArray()
                if (array.size == 2) {
                    title = formatTitle(array)[1]
                    artist = formatTitle(array)[0]
                }
                val songInfo = SongInfo(title, artist, url, duration)
                songList.add(songInfo)
            }
        }
        return SongAdapter(context, songList)
    }
}
