package io.github.raghavsatyadev.support.models.db.song_detail

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import io.github.raghavsatyadev.support.Constants.DB.Tables
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
@Entity(
    tableName = Tables.SONG_DETAIL_TABLE,
    indices = [
        Index(value = [Tables.SONG_DETAIL_LINK, Tables.SONG_DETAIL_OLD_TITLE], unique = true),
    ]
)
data class SongDetail(
    @Transient
    @SerializedName(Tables.SONG_DETAIL_ID)
    @get:PropertyName(Tables.SONG_DETAIL_ID)
    @set:PropertyName(
        Tables.SONG_DETAIL_ID
    )
    @ColumnInfo(Tables.SONG_DETAIL_ID)
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    var artist: String = "",
    var thumbnailHigh: String = "",
    var thumbnailLow: String = "",
    var title: String = "",
    var description: String = "",

    @SerializedName(Tables.SONG_DETAIL_LINK)
    @get:PropertyName(Tables.SONG_DETAIL_LINK)
    @set:PropertyName(Tables.SONG_DETAIL_LINK)
    @ColumnInfo(Tables.SONG_DETAIL_LINK)
    var link: String = "",

    @SerializedName(Tables.SONG_DETAIL_OLD_TITLE)
    @get:PropertyName(Tables.SONG_DETAIL_OLD_TITLE)
    @set:PropertyName(Tables.SONG_DETAIL_OLD_TITLE)
    @ColumnInfo(Tables.SONG_DETAIL_OLD_TITLE)
    var oldTitle: String,
) : Parcelable