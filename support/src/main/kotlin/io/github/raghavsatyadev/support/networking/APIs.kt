package io.github.raghavsatyadev.support.networking

import android.net.Uri
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.GsonExtensions.toGsonObject
import io.github.raghavsatyadev.support.models.general.YoutubeError
import io.github.raghavsatyadev.support.models.general.YoutubeSearchData
import io.ktor.client.call.body
import io.ktor.client.request.get

object APIs {

    // region Youtube
    private var YOUTUBE_SEARCH_URL =
        "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="

    suspend fun searchYoutube(query: String, key: String): YoutubeSearchData {
        try {
            val response = KtorUtil.httpClient.get(getYoutubeSearchURL(query, key))
            val body = response.body<YoutubeSearchData>()
            AppLog.loge(true, kotlinFileName, "searchYoutube", body, Exception())
            return body
        } catch (e: Exception) {
            val errorReason = getErrorReason(e)
            throw Exception(errorReason)
        }
    }

    private fun getErrorReason(e: Exception): String? {
        // extract content from e.message using regex
        val errorMessage = e.message
        if (errorMessage != null) {
            val regex = Regex("Text: \"([\\S\\s]*)\"")
            val matchResult = regex.find(errorMessage)
            val matchingGroups = matchResult?.groups
            if (matchingGroups != null && matchingGroups.size >= 2) {
                val extractedContent =
                    matchingGroups[1]?.value?.toGsonObject<YoutubeError>()?.error?.errors
                if (!extractedContent.isNullOrEmpty()) {
                    return extractedContent[0].reason
                }
            }
        }
        return null
    }

    private fun getYoutubeSearchURL(query: String, key: String): String {
        val encodedQuery = Uri.encode(
            query.replace(
                " ",
                "+"
            )
        )
        return "$YOUTUBE_SEARCH_URL${
            encodedQuery
        }&key=$key"
    }
    // endregion
}