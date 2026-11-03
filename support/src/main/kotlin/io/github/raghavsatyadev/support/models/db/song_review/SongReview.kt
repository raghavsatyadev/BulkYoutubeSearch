package io.github.raghavsatyadev.support.models.db.song_review

import androidx.annotation.Keep
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import io.github.raghavsatyadev.support.Constants.DB.Tables
import io.github.raghavsatyadev.support.database.RoomDBUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/**
 * Whether the found YouTube video is the same song as the local video. [REJECTED] is a match the
 * user does not want in the playlist. Stored by name, so new values go at the end.
 */
@Keep
enum class ReviewVerdict {
  MATCH,
  MISMATCH,
  REJECTED,
}

/**
 * One review of a found song. Keyed by [oldTitle] (the local file name), not by the song's id: the
 * id is regenerated when the links file is imported again, the file name is not.
 */
@Keep
@Serializable
@Entity(tableName = Tables.SONG_REVIEW_TABLE)
data class SongReview(
  @PrimaryKey val oldTitle: String,
  /** The full YouTube URL that was reviewed. */
  val link: String,
  val verdict: ReviewVerdict,
  /** Epoch milliseconds of the last change. */
  val reviewedAt: Long,
  /**
   * Full device path of the local video it was compared with, such as
   * "/storage/emulated/0/Movies/BYS/Hollywood/x.mp4"; empty when none was found.
   */
  val localPath: String = "",
)

@Dao
interface SongReviewDao {
  @Query("SELECT * FROM `${Tables.SONG_REVIEW_TABLE}`") fun getAllLive(): Flow<List<SongReview>>

  @Query("SELECT * FROM `${Tables.SONG_REVIEW_TABLE}` ORDER BY reviewedAt")
  suspend fun getAll(): List<SongReview>

  @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(review: SongReview)

  @Query("DELETE FROM `${Tables.SONG_REVIEW_TABLE}` WHERE oldTitle = :oldTitle")
  suspend fun delete(oldTitle: String)
}

object SongReviewDataUtil {
  val dao: SongReviewDao
    get() = RoomDBUtil.getDatabase().songReviewDao()
}
