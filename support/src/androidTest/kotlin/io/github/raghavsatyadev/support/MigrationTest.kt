package io.github.raghavsatyadev.support

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import io.github.raghavsatyadev.support.database.AppDatabase
import io.github.raghavsatyadev.support.database.MigrationUtil
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class MigrationTest {
  private val instrumentation = InstrumentationRegistry.getInstrumentation()
  private val dbFile = instrumentation.targetContext.getDatabasePath("migration-test")

  @get:Rule
  val helper: MigrationTestHelper =
    MigrationTestHelper(
      instrumentation = instrumentation,
      file = dbFile,
      driver = BundledSQLiteDriver(),
      databaseClass = AppDatabase::class,
    )

  @Test
  fun migrateAll() = runBlocking {
    // Start at v2: v1 was the template's schema and never shipped (the first release used v3).
    // Room then runs every later migration and checks the result against the current schema.
    helper.createDatabase(2).close()
    val db =
      Room.databaseBuilder<AppDatabase>(instrumentation.targetContext, dbFile.absolutePath)
        .setDriver(BundledSQLiteDriver())
        .addMigrations(*MigrationUtil.migrations)
        .build()
    db.songDetailDao().getCount(androidx.room3.RoomRawQuery("SELECT COUNT(*) FROM song_detail"))
    db.close()
  }
}
