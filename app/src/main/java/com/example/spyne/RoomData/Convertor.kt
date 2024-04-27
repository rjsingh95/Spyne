package com.example.spyne.RoomData

import androidx.room.TypeConverter
import java.util.*


public class Converter {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }


    @TypeConverter
    fun toDate(dateLong: Long?): Date? {
        return dateLong?.let { Date(it) }
    }

    companion object {}
}

