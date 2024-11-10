package io.github.raghavsatyadev.support.work_manager

import android.os.Environment
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toGsonObject
import io.github.raghavsatyadev.support.extensions.Randoms
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.general.APIKeyDetail
import io.github.raghavsatyadev.support.models.general.YoutubeSearchData
import io.github.raghavsatyadev.support.networking.APIs
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.io.IOException

object YoutubeSearch {
    private var apiKeys: ArrayList<APIKeyDetail> = ArrayList()

    private val parentFile =
        StorageUtils.getFilePathWithoutCreating("Videos").apply {
            mkdirs()
        }
    private val readerFile = File(parentFile, "video_names.json")
    private val resultFile: File = File(parentFile, "video_links.json")

    suspend fun searchVideos(): Boolean {
        val (isDataSetupCorrectly, remainingSongs) = setupData()

        if (isDataSetupCorrectly) {
            val remainingSongsReversed = ArrayList(remainingSongs.reversed())

            for ((index, oldSongDetail) in remainingSongsReversed.withIndex()) {
                try {
                    val response = searchYoutubeForVideo(oldSongDetail)
                    val songDetail = response.second
                    if (response.first) {
                        if (songDetail != null) {
                            handleYoutubeResult(songDetail)
                            remainingSongs.removeAt(remainingSongsReversed.size - index - 1)
                        }
                    } else {
                        break
                    }
                } catch (e: Exception) {
                    AppLog.loge(false, kotlinFileName, "searchVideos", e, Exception())
                    break
                }
            }
            return remainingSongs.isEmpty()
        } else {
            return false
        }
    }

    private suspend fun handleYoutubeResult(songDetail: SongDetail) {
        AppLog.loge(
            true,
            kotlinFileName,
            "searchVideos",
            "Title: ${songDetail.oldTitle}\nVideo: ${songDetail.link}",
            Exception()
        )
        AppPrefsUtil.setRemainingSongsToBeFound()
        SongDetailDataUtil.getInstance().update(songDetail)
    }

    suspend fun setupData(): Pair<Boolean, ArrayList<SongDetail>> {
        setupAPIKeys()

        val externalStoragePublicDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        externalStoragePublicDirectory?.let {
            val videoTitles = ArrayList<String>()
            val oldVideoData = ArrayList<SongDetail>()

            val readerExternalFile = File(it, readerFile.name)
            if (readerExternalFile.exists()) {
                videoTitles.addAll(readerExternalFile.readFile())
            }
            val resultExternalFile = File(it, resultFile.name)
            if (resultExternalFile.exists()) {
                oldVideoData.addAll(resultExternalFile.readFile())
            }
            videoTitles.forEach { videoTitle ->
                if (oldVideoData.find { data -> data.oldTitle == videoTitle } == null) {
                    oldVideoData.add(SongDetail(oldTitle = videoTitle))
                }
            }
            oldVideoData.sortBy { detail ->
                detail.oldTitle
            }
            SongDetailDataUtil.getInstance().upsert(oldVideoData)
        }
        val remainingTitles = SongDetailDataUtil.getInstance().getAllSorted(false)
        AppPrefsUtil.setRemainingSongsToBeFound(remainingTitles.size)

        return true to remainingTitles
    }

    private suspend fun setupAPIKeys() {
        apiKeys = AppPrefsUtil.getKeyDetails().first().ifEmpty {
            val keyJSONString = BuildConfig.youtube_api_keys
            val jsonKeyDetails =
                JSONArray(keyJSONString)
            val apiKeyDetails = ArrayList<APIKeyDetail>()
            for (i in 0 until jsonKeyDetails.length()) {
                val keyDetail = jsonKeyDetails.getJSONObject(i)
                val key = keyDetail.getString("key")
                val appName = keyDetail.getString("name")
                apiKeyDetails.add(
                    APIKeyDetail(
                        appName,
                        key,
                        System.currentTimeMillis()
                    )
                )
            }
            AppPrefsUtil.saveAllKeyDetail(apiKeyDetails)
            apiKeyDetails
        }
    }

    private suspend fun searchYoutubeForVideo(oldSongDetail: SongDetail): Pair<Boolean, SongDetail?> {
        if (apiKeys.isEmpty()) {
            return false to null
        } else {
            val currentKeyDetail = Randoms.selectRandomElement(*apiKeys.toTypedArray())
            val currentAPIKey = currentKeyDetail.key
            try {
                val searchData = APIs.searchYoutube(oldSongDetail.oldTitle, currentAPIKey)

                return buildSongDetailObject(searchData, oldSongDetail)
            } catch (e: Exception) {
                if (e.message == "quotaExceeded") {
                    AppPrefsUtil.saveKeyDetail(currentKeyDetail, System.currentTimeMillis())
                    apiKeys.remove(currentKeyDetail)
                    return searchYoutubeForVideo(oldSongDetail)
                } else {
                    return true to null
                }
            }
        }
    }

    private fun buildSongDetailObject(
        searchData: YoutubeSearchData,
        oldSongDetail: SongDetail,
    ): Pair<Boolean, SongDetail?> {
        val items = searchData.items

        if (items.isEmpty()) {
            val e = Exception("No videos found for query: ${oldSongDetail.oldTitle}")
            AppLog.loge(false, kotlinFileName, "searchYouTube", e, e)
            return true to null
        } else {
            val video = items[0]
            val videoLink = "https://www.youtube.com/watch?v=${video.id.videoId}"
            val videoTitle = video.snippet.title
            val description = video.snippet.description
            val artist = video.snippet.channelTitle
            val thumbnailHigh = video.snippet.thumbnails.high.url
            val thumbnailLow = video.snippet.thumbnails.default.url

            with(oldSongDetail) {
                this.link = videoLink
                this.title = videoTitle
                this.description = description
                this.artist = artist
                this.thumbnailHigh = thumbnailHigh
                this.thumbnailLow = thumbnailLow
            }

            return true to oldSongDetail
        }
    }

    private inline fun <reified T> File.readFile(): ArrayList<T> {
        try {
            if (!exists()) {
                createNewFile()
                FileWriter(this).use { writer ->
                    writer.write("[]")
                }
            }

            val readStringFromFile = readText()
            if (readStringFromFile.isEmpty()) {
                return ArrayList()
            }
            return readStringFromFile.toGsonObject()
        } catch (e: IOException) {
            AppLog.loge(false, this@YoutubeSearch.kotlinFileName, "getVideoTitles", e, Exception())
        }
        return ArrayList()
    }
}

