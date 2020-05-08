package com.dkarakaya.songify.util

import com.commit451.youtubeextractor.Stream
import com.commit451.youtubeextractor.Thumbnail
import com.commit451.youtubeextractor.YouTubeExtraction

object YoutubeExtractionModelSample {
    fun youTubeExtraction(
            videoId: String = "12",
            title: String = "Kendrick Lamar - DNA",
            streams: List<Stream> = emptyList(),
            thumbnails: List<Thumbnail> = emptyList(),
            author: String = "Kendrick Lamar",
            description: String? = "DAMN. available now http://smarturl.it/DAMN\n Dir: Nabil & the little homies\n Producer: Anthony “Top Dawg” Tiffith, Dave Free, Angel J Rosa\n Production co: TDE Films, AJR Films\n (C) 2017 Aftermath/Interscope (Top Dawg Entertainment)\n http://vevo.ly/l2Qp5O\n Best of Kendrick Lamar: https://goo.gl/PTr3FF\n Subscribe here: https://goo.gl/XGVyCd Category Music",
            viewCount: Long? = 219499765L,
            durationMilliseconds: Long = 20000L
    ): YouTubeExtraction {
        return YouTubeExtraction(videoId = videoId,
                title = title,
                streams = streams,
                thumbnails = thumbnails,
                author = author,
                description = description,
                viewCount = viewCount,
                durationMilliseconds = durationMilliseconds
        )
    }
}
