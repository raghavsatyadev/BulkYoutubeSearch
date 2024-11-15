package io.github.raghavsatyadev.support.models.db.song_detail

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import io.github.raghavsatyadev.support.Constants
import io.github.raghavsatyadev.support.database.BaseDao
import io.github.raghavsatyadev.support.database.BaseDataUtil
import io.github.raghavsatyadev.support.database.RoomDBUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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

    fun getAllSortedLive(retrievalMode: SongRetrievalMode = SongRetrievalMode.FOUND): Flow<ArrayList<SongDetail>> {
        val foundQueryPart = buildLinkFilter(retrievalMode)
        val selectQueryPart = "SELECT * FROM `${getTableName()}`"
        val orderQueryPart =
            " ORDER BY `${buildOrderByPart(retrievalMode)}` COLLATE NOCASE"
        val query = SimpleSQLiteQuery(
            "$selectQueryPart$foundQueryPart$orderQueryPart"
        )
        return getDao().getAllLive(query).map { it as ArrayList<SongDetail> }
    }

    private fun buildOrderByPart(retrievalMode: SongRetrievalMode): String {
        return when (retrievalMode) {
            SongRetrievalMode.FOUND -> "title"
            SongRetrievalMode.NOT_FOUND -> Constants.DB.Tables.SONG_DETAIL_OLD_TITLE
            SongRetrievalMode.ALL -> Constants.DB.Tables.SONG_DETAIL_OLD_TITLE
        }
    }

    private fun buildLinkFilter(retrievalMode: SongRetrievalMode): String {
        val foundQueryPart = when (retrievalMode) {
            SongRetrievalMode.FOUND -> " WHERE `link` != ''"
            SongRetrievalMode.NOT_FOUND -> " WHERE `link` == ''"
            SongRetrievalMode.ALL -> ""
        }
        return foundQueryPart
    }

    suspend fun getAllSorted(retrievalMode: SongRetrievalMode = SongRetrievalMode.FOUND): ArrayList<SongDetail> {
        return getAllSortedLive(retrievalMode).first()
    }

    fun upsert(songDetails: ArrayList<SongDetail>) {
        getDao().upsert(songDetails)
    }

    fun getCountLive(retrievalMode: SongRetrievalMode = SongRetrievalMode.FOUND): Flow<Long> {
        return getDao().getCountLive(
            SimpleSQLiteQuery(
                "SELECT COUNT(`link`) FROM `${getTableName()}`${
                    buildLinkFilter(
                        retrievalMode
                    )
                }"
            )
        )
    }

    @Dao
    abstract class SongDetailDao : BaseDao<SongDetail>() {
        @Transaction
        open fun upsert(songDetails: ArrayList<SongDetail>) {
            songDetails.forEach { songDetail ->
                val existingSongDetail = getItemByOldTitle(songDetail.oldTitle)
                if (existingSongDetail != null) {
                    songDetail.id = existingSongDetail.id
                    if (songDetail.link.isNotEmpty()) {
                        update(songDetail)
                    }
                } else {
                    insertIgnore(songDetail)
                }
            }
        }

        @Query("SELECT * FROM ${Constants.DB.Tables.SONG_DETAIL_TABLE} WHERE ${Constants.DB.Tables.SONG_DETAIL_OLD_TITLE} = :oldTitle LIMIT 1")
        abstract fun getItemByOldTitle(oldTitle: String): SongDetail?

        @RawQuery(observedEntities = [SongDetail::class])
        abstract fun getAllLive(simpleSQLiteQuery: SimpleSQLiteQuery): Flow<List<SongDetail>>

        @RawQuery(observedEntities = [SongDetail::class])
        abstract fun getCountLive(supportSQLiteQuery: SupportSQLiteQuery): Flow<Long>
    }
}

enum class SongRetrievalMode {
    FOUND,
    NOT_FOUND,
    ALL
}