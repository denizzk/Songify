package com.dkarakaya.songify

import android.os.Parcel
import android.os.Parcelable

data class YoutubeDataModel(var title: String? = "",
                            var channelTitle: String? = "",
                            var publishedAt: String? = "",
                            var thumbnail: String? = "",
                            var videoId: String? = "") : Parcelable {


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(channelTitle)
        dest.writeString(publishedAt)
        dest.writeString(thumbnail)
        dest.writeString(videoId)
    }

    private constructor(parcel: Parcel) : this() {
        readFromParcel(parcel)
    }

    private fun readFromParcel(parcel: Parcel) {
        title = parcel.readString()
        channelTitle = parcel.readString()
        publishedAt = parcel.readString()
        thumbnail = parcel.readString()
        videoId = parcel.readString()
    }

    companion object {
        val CREATOR: Parcelable.Creator<YoutubeDataModel> = object : Parcelable.Creator<YoutubeDataModel> {
            override fun createFromParcel(parcel: Parcel): YoutubeDataModel? {
                return YoutubeDataModel(parcel)
            }

            override fun newArray(size: Int): Array<YoutubeDataModel?> {
                return arrayOfNulls(size)
            }
        }
    }
}
