package io.github.raghavsatyadev.support.database

import androidx.room.Room
import androidx.room.RoomDatabase.Callback
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.models.db.song_detail.SongDetailDataUtil
import kotlinx.coroutines.launch

object RoomDBUtil {
    @Volatile
    private var database: AppDatabase? = null

    @Synchronized
    fun getDatabase(): AppDatabase {
        if (database == null) {
            database =
                Room.databaseBuilder(CoreApp.instance, AppDatabase::class.java, Constants.DB.NAME)
                    .allowMainThreadQueries()
                    .addMigrations(*MigrationUtil.migrations)
                    .fallbackToDestructiveMigration(true)
                    .addCallback(object : Callback() {
                    })
                    .build()
        }
        return database!!
    }

    fun deleteAll() {
        CoreApp.instance.launch {
            SongDetailDataUtil.getInstance().deleteAll()
        }
    }
}