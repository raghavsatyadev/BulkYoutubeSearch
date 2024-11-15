package io.github.raghavsatyadev.support.models.db.song_detail

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.github.raghavsatyadev.support.Constants.DB.Tables
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Keep
@Parcelize
@Serializable
@Entity(
    tableName = Tables.SONG_DETAIL_TABLE,
    indices = [
        Index(value = [Tables.SONG_DETAIL_LINK, Tables.SONG_DETAIL_OLD_TITLE], unique = true),
    ]
)
data class SongDetail(
    @Transient
    @SerialName(Tables.SONG_DETAIL_ID)
    @ColumnInfo(Tables.SONG_DETAIL_ID)
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var artist: String = "",
    var thumbnailHigh: String = "",
    var thumbnailLow: String = "",
    var title: String = "",
    var description: String = "",

    @SerialName(Tables.SONG_DETAIL_LINK)
    @ColumnInfo(Tables.SONG_DETAIL_LINK)
    var link: String = "",

    @SerialName(Tables.SONG_DETAIL_OLD_TITLE)
    @ColumnInfo(Tables.SONG_DETAIL_OLD_TITLE)
    var oldTitle: String,
) : Parcelable