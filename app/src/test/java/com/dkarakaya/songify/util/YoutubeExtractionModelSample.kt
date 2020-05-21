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
            description: String? = "DAMN. available now http://smarturl.it/DAMN",
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
