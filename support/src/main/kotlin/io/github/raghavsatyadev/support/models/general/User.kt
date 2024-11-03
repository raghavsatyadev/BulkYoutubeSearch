package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import io.github.raghavsatyadev.support.Constants
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class User(
    var email: String,
    var name: String,

    @SerializedName(Constants.DB.Tables.USER_ID)
    @get:PropertyName(Constants.DB.Tables.USER_ID)
    @set:PropertyName(Constants.DB.Tables.USER_ID)
    @ColumnInfo(Constants.DB.Tables.USER_ID)
    var userID: String,
) : Parcelable