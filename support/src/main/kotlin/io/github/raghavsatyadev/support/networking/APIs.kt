package io.github.raghavsatyadev.support.networking

import androidx.core.text.HtmlCompat
import io.github.raghavsatyadev.support.AppLog
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
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
            return response.body<YoutubeSearchData>()
        } catch (e: Exception) {
            val errorReason = getErrorReason(e)
            AppLog.loge(true, kotlinFileName, "searchYoutube", errorReason, Exception())
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
                    matchingGroups[1]?.value?.toKotlinObject<YoutubeError>()?.error?.errors
                if (!extractedContent.isNullOrEmpty()) {
                    return extractedContent[0].reason
                }
            }
        }
        return null
    }

    private fun getYoutubeSearchURL(query: String, key: String): String {
        val encodedQuery =
            HtmlCompat.fromHtml(query, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
                .replace(" ", "+")
        return "$YOUTUBE_SEARCH_URL${
            encodedQuery
        }&key=$key"
    }
    // endregion
}