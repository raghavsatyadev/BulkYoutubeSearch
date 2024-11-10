package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class YoutubeSearchData(
    var kind: String,
    var etag: String,
    var nextPageToken: String,
    var regionCode: String,
    var pageInfo: PageInfo,
    var items: List<Item>,
) : Parcelable

@Parcelize
data class PageInfo(
    var totalResults: Int,
    var resultsPerPage: Int,
) : Parcelable

@Parcelize
data class Item(
    var kind: String,
    var etag: String,
    var id: Id,
    var snippet: Snippet,
) : Parcelable

@Parcelize
data class Id(
    var kind: String,
    var videoId: String,
) : Parcelable

@Parcelize
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
data class Thumbnails(
    var default: Default,
    var medium: Medium,
    var high: High,
) : Parcelable

@Parcelize
data class Default(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable

@Parcelize
data class Medium(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable

@Parcelize
data class High(
    var url: String,
    var width: Int,
    var height: Int,
) : Parcelable


