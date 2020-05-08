package com.dkarakaya.songify.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class YoutubeVideoModel(
        var title: String?,
        var channelTitle: String?,
        var publishedAt: String?,
        var thumbnail: String?,
        var videoId: String
) : Parcelable
