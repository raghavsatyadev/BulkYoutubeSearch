package io.github.raghavsatyadev.support.database

import androidx.room3.RoomRawQuery

@Suppress("unused")
abstract class BaseDataUtil<T, D : BaseDao<T>> {
  abstract fun getDao(): D

  abstract fun getTableName(): String

  abstract fun getPrimaryKey(): String

  open suspend fun insertReplace(ts: List<T>): List<Long> = getDao().insertReplace(ts)

  open suspend fun insertReplace(t: T): Long = getDao().insertReplace(t)

  open suspend fun insertIgnore(ts: List<T>): List<Long> = getDao().insertIgnore(ts)

  open suspend fun insertIgnore(t: T): Long = getDao().insertIgnore(t)

  open suspend fun update(t: T): Int = getDao().update(t)

  open suspend fun update(ts: List<T>): Int = getDao().update(ts)

  open suspend fun delete(id: Long): Int =
    getDao()
      .delete(RoomRawQuery("DELETE FROM `${getTableName()}` WHERE `${getPrimaryKey()}` = $id"))

  open suspend fun delete(t: T): Int = getDao().delete(t)

  open suspend fun delete(ts: List<T>): Int = getDao().delete(ts)

  open suspend fun delete(primaryKeyId: String): Int =
    getDao()
      .delete(
        RoomRawQuery("DELETE FROM `${getTableName()}` WHERE `${getPrimaryKey()}` LIKE ?") {
          it.bindText(1, primaryKeyId)
        }
      )

  open suspend fun deleteAll(): Int =
    getDao().delete(RoomRawQuery("DELETE FROM `${getTableName()}`"))

  open suspend fun replaceAll(ts: List<T>): List<Long> {
    deleteAll()
    return insertReplace(ts)
  }

  fun buildGetAllSortedQuery(sortKey: String): String {
    val sortQuery = if (sortKey.isEmpty()) "" else " ORDER BY `$sortKey`"
    return "SELECT * FROM `${getTableName()}`$sortQuery"
  }

  open suspend fun getAll(sortKey: String = ""): ArrayList<T> =
    ArrayList(getDao().getAll(RoomRawQuery(buildGetAllSortedQuery(sortKey))))

  open suspend fun getItem(id: String): T =
    getDao()
      .getItem(
        RoomRawQuery(
          "SELECT * FROM `${getTableName()}` WHERE `${getPrimaryKey()}` LIKE ? LIMIT 1"
        ) {
          it.bindText(1, id)
        }
      )

  open suspend fun getItem(id: Long): T =
    getDao()
      .getItem(
        RoomRawQuery("SELECT * FROM `${getTableName()}` WHERE `${getPrimaryKey()}` = $id LIMIT 1")
      )

  open suspend fun getCount(): Long =
    getDao().getCount(RoomRawQuery("SELECT COUNT(`${getPrimaryKey()}`) FROM `${getTableName()}`"))
}
