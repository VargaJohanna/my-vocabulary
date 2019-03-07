package com.vocabulary.myvocabulary.utils

import androidx.room.TypeConverter
import java.util.*


class DateTypeConverter {
    @TypeConverter
    fun toDate(value: Long): Date = Date(value)

    @TypeConverter
    fun toLong(value: Date): Long = value.time
}