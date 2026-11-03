package io.github.raghavsatyadev.support.database

import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
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
@ColumnTypeConverters(value = [EssentialConverters::class])
abstract class AppDatabase : RoomDatabase() {
  abstract fun songDetailDao(): SongDetailDataUtil.SongDetailDao

  abstract fun songReviewDao(): SongReviewDao
}
