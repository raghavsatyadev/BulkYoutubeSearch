package io.github.raghavsatyadev.support.models.db.song_detail

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
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

    fun getAllSortedLive(retrieveFound: Boolean = true): Flow<ArrayList<SongDetail>> {
        val foundQueryPart = if (retrieveFound) " WHERE `link` != ''" else ""
        val selectQueryPart = "SELECT * FROM `${getTableName()}`"
        val orderQueryPart =
            " ORDER BY `${if (retrieveFound) "title" else Constants.DB.Tables.SONG_DETAIL_OLD_TITLE}` COLLATE NOCASE"
        val query = SimpleSQLiteQuery(
            "$selectQueryPart$foundQueryPart$orderQueryPart"
        )
        return getDao().getAllLive(query).map { it as ArrayList<SongDetail> }
    }

    suspend fun getAllSorted(retrieveFound: Boolean = true): ArrayList<SongDetail> {
        return getAllSortedLive(retrieveFound).first()
    }

    fun upsert(songDetails: ArrayList<SongDetail>) {
        getDao().upsert(songDetails)
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
    }
}