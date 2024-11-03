package io.github.raghavsatyadev.support.models.db.song_detail

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.database.BaseDao
import io.github.raghavsatyadev.support.database.BaseDataUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SongDetailDataUtil : BaseDataUtil<SongDetail, SongDetailDataUtil.SongDetailDao>() {
    companion object {
        @Volatile
        private var instance: SongDetailDataUtil? = null

        @Synchronized
        fun getInstance(): SongDetailDataUtil {
            if (instance == null) instance = SongDetailDataUtil()
            return instance!!
        }
    }

    override fun getDao(): SongDetailDao {
        return RoomDBUtil.getDatabase().songDetailDao()
    }

    override fun getTableName(): String {
        return Constants.DB.Tables.SONG_DETAIL_TABLE
    }

    override fun getPrimaryKey(): String {
        return Constants.DB.Tables.SONG_DETAIL_ID
    }

    fun getAllLive(): Flow<ArrayList<SongDetail>> {
        return getDao().getAllLive(SimpleSQLiteQuery("SELECT * FROM `${getTableName()}`"))
            .map { value: List<SongDetail> -> ArrayList(value) }
    }

    @Dao
    abstract class SongDetailDao : BaseDao<SongDetail>() {
        @RawQuery(observedEntities = [SongDetail::class])
        abstract fun getAllLive(simpleSQLiteQuery: SimpleSQLiteQuery): Flow<List<SongDetail>>
    }
}