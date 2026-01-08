package com.vocabulary.myvocabulary.utils

import android.os.Build
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class DateTypeConverter {
    @TypeConverter
    fun toDate(value: Long): Date = Date(value)

    @TypeConverter
    fun toLong(value: Date): Long = value.time

    fun formatDate(date: Date): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy")
            date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(formatter).toString()
        } else {
            val dateFormat = SimpleDateFormat.getDateInstance()
            dateFormat.format(date).toString()
        }
    }
}