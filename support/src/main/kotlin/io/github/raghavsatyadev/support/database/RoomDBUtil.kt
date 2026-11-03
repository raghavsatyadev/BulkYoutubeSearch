package io.github.raghavsatyadev.support.database

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RoomDBUtil {
  @Volatile private var database: AppDatabase? = null

  @Synchronized
  fun getDatabase(): AppDatabase {
    if (database == null) {
      // The same file the Room 2 builds used, so an update keeps the found songs.
      val dbFile = CoreApp.instance.getDatabasePath(Constants.DB.NAME)
      database =
        Room.databaseBuilder<AppDatabase>(CoreApp.instance, name = dbFile.absolutePath)
          .addMigrations(*MigrationUtil.migrations)
          .fallbackToDestructiveMigration(true)
          .setDriver(BundledSQLiteDriver())
          .setQueryCoroutineContext(Dispatchers.IO)
          .build()
    }
    return database!!
  }

  fun deleteAll() {
    CoreApp.instance.launch { SongDetailDataUtil.getInstance().deleteAll() }
  }
}
