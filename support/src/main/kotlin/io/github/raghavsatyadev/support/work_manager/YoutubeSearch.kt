package io.github.raghavsatyadev.support.work_manager

import android.os.Environment
import com.google.gson.JsonObject
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.BuildConfig
import io.github.raghavsatyadev.support.StorageUtils
import io.github.raghavsatyadev.support.StorageUtils.readStringFromFile
import io.github.raghavsatyadev.support.StorageUtils.writeStringToFile
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toGsonObject
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.Randoms
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.general.YoutubeSearchData
import io.github.raghavsatyadev.support.preferences.AppPrefsUtil
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner

object YoutubeSearch {
    private var API_KEYS: ArrayList<AppPrefsUtil.APIKeyDetail> = ArrayList()
    private val READER_FILE = StorageUtils.getFilePathWithoutCreating("Videos", "video_names.json")
    val RESULT_FILE: File = StorageUtils.getFilePathWithoutCreating("Videos", "video_links.json")

    private const val BASE_URL =
        "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="

    suspend fun searchVideos(): Boolean {
        setupAPIKeys()

        val videoTitles = ArrayList<String>()
        val oldVideoData = ArrayList<SongDetail>()
        try {
            if (!READER_FILE.exists()) {
                copyVideoFileFromDownloads()
            }
            if (RESULT_FILE.exists()) {
                oldVideoData.addAll(readFiles(RESULT_FILE))
            }
            videoTitles.addAll(readFiles(READER_FILE))
        } catch (e: IOException) {
            AppLog.loge(false, kotlinFileName, "searchVideos", e, Exception())
            return false
        }

        if (oldVideoData.isNotEmpty()) {
            SongDetailDataUtil.getInstance().insertIgnore(oldVideoData)
            val oldTitleSize = videoTitles.size
            oldVideoData.forEach { data ->
                val remove = videoTitles.remove(data.oldTitle)
                AppLog.loge(true, kotlinFileName, "searchVideos", remove, Exception())
            }
            if (oldTitleSize != videoTitles.size) {
                READER_FILE.writeStringToFile(videoTitles.toJsonString())
            }
        }
        AppPrefsUtil.setRemainingSongsToBeFound(videoTitles.size)
        val videoLinkResults = ArrayList<SongDetail>()

        for ((index, title) in videoTitles.reversed().withIndex()) {
            try {
                val response = searchYoutubeForVideo(title)
                val songDetail = response.second
                if (response.first) {
                    if (songDetail != null) {
                        AppLog.loge(
                            true,
                            kotlinFileName,
                            "searchVideos",
                            "Title: $title\nVideo: ${songDetail.link}",
                            Exception()
                        )
                        AppPrefsUtil.setRemainingSongsToBeFound()
                        SongDetailDataUtil.getInstance().insertIgnore(oldVideoData)
                        videoLinkResults.add(songDetail)
                        videoTitles.removeAt(index)
                    }
                } else {
                    break
                }
            } catch (e: Exception) {
                AppLog.loge(false, kotlinFileName, "searchVideos", e, Exception())
                break
            }
        }
        READER_FILE.writeStringToFile(videoTitles.toJsonString())
        videoLinkResults.addAll(oldVideoData)
        videoLinkResults.sortedWith(compareBy<SongDetail> { it.artist }.thenBy { it.title })
        RESULT_FILE.writeStringToFile(videoLinkResults.toJsonString())
        return videoTitles.isEmpty()
    }

    private fun copyVideoFileFromDownloads() {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.let {
            val file = File(it, "video_names.json")
            if (file.exists()) {
                file.copyTo(READER_FILE, true)
            }
        }
    }

    private suspend fun setupAPIKeys() {
        API_KEYS = AppPrefsUtil.getKeyDetails().first().ifEmpty {
            val keyJSONString = BuildConfig.youtube_api_keys
            val jsonKeyDetails =
                JSONArray(keyJSONString)
            val apiKeyDetails = ArrayList<AppPrefsUtil.APIKeyDetail>()
            for (i in 0 until jsonKeyDetails.length()) {
                val keyDetail = jsonKeyDetails.getJSONObject(i)
                val key = keyDetail.getString("key")
                val appName = keyDetail.getString("name")
                apiKeyDetails.add(
                    AppPrefsUtil.APIKeyDetail(
                        appName,
                        key,
                        System.currentTimeMillis()
                    )
                )
            }
            apiKeyDetails
        }
    }

    private suspend fun searchYoutubeForVideo(query: String): Pair<Boolean, SongDetail?> {
        if (API_KEYS.isEmpty()) {
            return false to null
        } else {
            val currentKeyDetail = Randoms.selectRandomElement(*API_KEYS.toTypedArray())
            val currentAPIKey = currentKeyDetail.key
            val searchUrl = BASE_URL + query.replace(" ", "+") + "&key=" + currentAPIKey
            val response = requestForSongLink(searchUrl)
            if (!response.first) {
                AppPrefsUtil.saveKeyDetail(currentKeyDetail, System.currentTimeMillis())
                API_KEYS.remove(currentKeyDetail)
                return true to null
            } else if (response.second == null) {
                return true to null
            } else {
                val items = response.second?.getAsJsonArray("items")
                if (items == null) {
                    return true to null
                } else {
                    if (items.isEmpty) {
                        val e = Exception("No videos found for query: $query")
                        AppLog.loge(false, kotlinFileName, "searchYouTube", e, e)
                        return true to null
                    } else {
                        val video = items.toString().toGsonObject<ArrayList<YoutubeSearchData>>()[0]
                        val videoLink = "https://www.youtube.com/watch?v=${video.id.videoId}"
                        val videoTitle = video.snippet.title
                        val artist = video.snippet.channelTitle
                        val thumbnail = video.snippet.thumbnails.high.url
                        return true to SongDetail(
                            artist = artist,
                            link = videoLink,
                            thumbnail = thumbnail,
                            title = videoTitle,
                            oldTitle = query
                        )
                    }
                }
            }
        }
    }

    private fun requestForSongLink(searchUrl: String): Pair<Boolean, JsonObject?> {
        try {
            val url = URL(searchUrl)

            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connect()

            val responseCode = conn.responseCode
            if (responseCode != 200) {
                val e = Exception("Failed to get response from YouTube API: $responseCode")
                AppLog.loge(false, kotlinFileName, "requestForSongLink", e, e)
                return false to null
            }

            val scanner = Scanner(url.openStream())
            val jsonResponse = StringBuilder()
            while (scanner.hasNext()) {
                jsonResponse.append(scanner.nextLine())
            }
            scanner.close()

            return true to jsonResponse.toString().toGsonObject()
        } catch (e: Exception) {
            AppLog.loge(false, kotlinFileName, "requestForSongLink", e, e)
            return false to null
        }
    }

    private inline fun <reified T> readFiles(file: File): ArrayList<T> {
        try {
            if (!file.exists()) {
                file.createNewFile()
                FileWriter(file).use { writer ->
                    writer.write("[]")
                }
            }

            val readStringFromFile = file.readStringFromFile()
            if (readStringFromFile.isEmpty()) {
                return ArrayList()
            }
            return readStringFromFile.toGsonObject()
        } catch (e: IOException) {
            AppLog.loge(false, kotlinFileName, "getVideoTitles", e, Exception())
        }
        return ArrayList()
    }
}

