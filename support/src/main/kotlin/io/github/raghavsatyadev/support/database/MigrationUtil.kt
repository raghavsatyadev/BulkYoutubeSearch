package io.github.raghavsatyadev.support.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.raghavsatyadev.support.Constants

object MigrationUtil {

  private val MIGRATION_1_2: Migration =
    object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {}
    }
  /** Adds the release date. Without it the destructive fallback would wipe every found song. */
  private val MIGRATION_2_3: Migration =
    object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          "ALTER TABLE `${Constants.DB.Tables.SONG_DETAIL_TABLE}` ADD COLUMN " +
            "`${Constants.DB.Tables.SONG_DETAIL_PUBLISHED_AT}` TEXT NOT NULL DEFAULT ''"
        )
      }
    }

  /** Adds the table of song reviews; the found songs stay as they are. */
  private val MIGRATION_3_4: Migration =
    object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          "CREATE TABLE IF NOT EXISTS `${Constants.DB.Tables.SONG_REVIEW_TABLE}` (" +
            "`oldTitle` TEXT NOT NULL, `link` TEXT NOT NULL, `verdict` TEXT NOT NULL, " +
            "`reviewedAt` INTEGER NOT NULL, `localPath` TEXT NOT NULL, PRIMARY KEY(`oldTitle`))"
        )
      }
    }

  val migrations: Array<Migration>
    get() = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
}
