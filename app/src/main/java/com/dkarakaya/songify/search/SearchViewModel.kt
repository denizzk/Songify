package com.dkarakaya.songify.search

import android.os.HandlerThread
import androidx.lifecycle.ViewModel
import com.commit451.youtubeextractor.Stream
import com.commit451.youtubeextractor.YouTubeExtraction
import com.commit451.youtubeextractor.YouTubeExtractor
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


class SearchViewModel : ViewModel() {

    private val disposable = CompositeDisposable()

    private val videoIdInput = PublishSubject.create<String>()

    private val extractionResultSubject = PublishSubject.create<YouTubeExtraction>()
    private val titleOutput = PublishSubject.create<String>()
    private val streamOutput = PublishSubject.create<Stream>()
    private val downloadOutput = PublishSubject.create<Pair<String, String>>()

    init {
        val extractor: YouTubeExtractor = YouTubeExtractor
                .Builder()
                .build()

        // this thread is needed for extraction
        val handlerThread = HandlerThread("backgroundThread")
        if (!handlerThread.isAlive) handlerThread.start()

        // extraction result
        videoIdInput
                .observeOn(AndroidSchedulers.from(handlerThread.looper))
                .switchMap { id ->
                    Observable.just(extractor.extract(id).blockingGet())
                }
                .subscribeBy(
                        onNext = extractionResultSubject::onNext,
                        onError = Timber::e
                ).addTo(disposable)

        // title
        extractionResultSubject
                .map { it.title!! }
                .subscribeBy(
                        onNext = titleOutput::onNext,
                        onError = Timber::e
                ).addTo(disposable)

        // streams
        extractionResultSubject
                .flatMapIterable { extractionResult -> extractionResult.streams }
                .filter { stream -> (stream is Stream.VideoStream && !stream.isVideoOnly) || stream is Stream.AudioStream }
                .subscribeBy(
                        onNext = streamOutput::onNext,
                        onError = Timber::e
                ).addTo(disposable)

        // download
        extractionResultSubject
                .withLatestFrom(
                        streamOutput,
                        titleOutput
                ) { _, stream, title ->
                    when (stream) {
                        is Stream.VideoStream -> stream.url to title
                        is Stream.AudioStream -> stream.url to title
                    }
                }
                .subscribeBy(
                        onNext = downloadOutput::onNext,
                        onError = Timber::e
                ).addTo(disposable)

    }

    override fun onCleared() {
        super.onCleared()
        disposable.dispose()
    }

    /**
     * Input
     */
    fun videoId(id: String) {
        videoIdInput.onNext(id)
    }


    /**
     * Output
     */
    fun extractionResult(): PublishSubject<YouTubeExtraction> = extractionResultSubject
    fun title(): PublishSubject<String> = titleOutput
    fun streams(): PublishSubject<Stream> = streamOutput
    fun download(): PublishSubject<Pair<String, String>> = downloadOutput
}
