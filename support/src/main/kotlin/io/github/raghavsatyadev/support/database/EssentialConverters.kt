package io.github.raghavsatyadev.support.database

import androidx.room3.ColumnTypeConverter
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toJsonString
import io.github.raghavsatyadev.support.extensions.serializer.SerializationExtensions.toKotlinObject
import java.util.Date

@Suppress("unused")
class EssentialConverters {
  // region Essential Converters
  @ColumnTypeConverter
  fun fromDate(date: Date?): Long? {
    return date?.time
  }

  @ColumnTypeConverter
  fun toDate(millis: Long?): Date? {
    return millis?.let { Date(millis) }
  }

  @ColumnTypeConverter
  fun fromIntegerArrayList(data: ArrayList<Int>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toIntegerArrayList(s: String?): ArrayList<Int> {
    return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromIntegerList(data: List<Int>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toIntegerList(s: String?): List<Int> {
    return s?.toKotlinObject<ArrayList<Int>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromStringArrayList(data: ArrayList<String>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toStringArrayList(s: String?): ArrayList<String> {
    return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
  }

  @ColumnTypeConverter
  fun fromStringList(data: List<String>?): String {
    return data?.toJsonString() ?: "[]"
  }

  @ColumnTypeConverter
  fun toStringList(s: String?): List<String> {
    return s?.toKotlinObject<ArrayList<String>>() ?: ArrayList()
  }
  // endregion
}
