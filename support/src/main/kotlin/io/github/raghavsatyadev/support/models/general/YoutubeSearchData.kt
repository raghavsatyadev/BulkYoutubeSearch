package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Keep
@Parcelize
@Serializable
data class YoutubeSearchData(
    var kind: String,
    var etag: String,
    var nextPageToken: String,
    var regionCode: String,
    var pageInfo: PageInfo,
    var items: List<Item>,
) : Parcelable

@Parcelize
@Serializable
data class PageInfo(
    var totalResults: Int,
    var resultsPerPage: Int,
) : Parcelable

@Parcelize
@Serializable
data class Item(
    var kind: String,
    var etag: String,
    var id: Id,
    var snippet: Snippet,
) : Parcelable

@Parcelize
@Serializable
data class Id(
    var kind: String,
    var videoId: String,
) : Parcelable

@Parcelize
@Serializable
data class Snippet(
    var publishedAt: String,
    var channelId: String,
    var title: String,
    var description: String,
    var thumbnails: Thumbnails,
    var channelTitle: String,
    var liveBroadcastContent: String,
    var publishTime: String,
) : Parcelable

@Parcelize
@Serializable
data class Thumbnails(
    var default: Default,
    var medium: Medium,
    var high: High,
) : Parcelable

@Parcelize
@Serializable
data class Default(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable

@Parcelize
@Serializable
data class Medium(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable

@Parcelize
@Serializable
data class High(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable


