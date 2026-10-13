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

  val migrations: Array<Migration>
    get() = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
