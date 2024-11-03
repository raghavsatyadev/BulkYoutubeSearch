package io.github.raghavsatyadev.support.models.general

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.room.ColumnInfo
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName
import io.github.raghavsatyadev.support.Constants.FirebaseConstants
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class User(
    var email: String,
    var name: String,

    @SerializedName(FirebaseConstants.Fields.USER_ID)
    @get:PropertyName(FirebaseConstants.Fields.USER_ID)
    @set:PropertyName(FirebaseConstants.Fields.USER_ID)
    @ColumnInfo(FirebaseConstants.Fields.USER_ID)
    var userID: String,
) : Parcelable