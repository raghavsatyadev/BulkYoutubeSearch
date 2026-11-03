package io.github.raghavsatyadev.support.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetail
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import io.github.raghavsatyadev.support.models.db.song_review.SongReview
import io.github.raghavsatyadev.support.models.db.song_review.SongReviewDao

@Database(
  version = Constants.DB.VERSION,
  entities = [SongDetail::class, SongReview::class],
  autoMigrations = [],
)
@TypeConverters(value = [EssentialConverters::class])
abstract class AppDatabase : RoomDatabase() {
  abstract fun songDetailDao(): SongDetailDataUtil.SongDetailDao

  abstract fun songReviewDao(): SongReviewDao
}
