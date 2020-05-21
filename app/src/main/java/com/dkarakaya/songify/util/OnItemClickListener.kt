package com.dkarakaya.songify.util

import com.dkarakaya.songify.model.YoutubeItem

interface OnItemClickListener {
    fun onItemClick(item: YoutubeItem?)
}
