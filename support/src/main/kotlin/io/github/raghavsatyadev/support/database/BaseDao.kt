package io.github.raghavsatyadev.support.database

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

abstract class BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertReplace(t: T): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertReplace(ts: List<T>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIgnore(t: T): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertIgnore(ts: List<T>): List<Long>

    @Update
    abstract fun update(t: T): Int

    @Update
    abstract fun update(t: List<T>): Int

    @Delete
    abstract fun delete(t: T): Int

    @Delete
    abstract fun delete(t: List<T>): Int

    @RawQuery
    abstract fun delete(query: SimpleSQLiteQuery): Int

    @RawQuery
    abstract fun getAll(query: SupportSQLiteQuery): List<T>

    @RawQuery
    abstract fun getItem(query: SupportSQLiteQuery): T

    @RawQuery
    abstract fun getCount(supportSQLiteQuery: SupportSQLiteQuery): Long
}