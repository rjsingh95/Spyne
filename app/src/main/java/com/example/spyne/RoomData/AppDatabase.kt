package com.example.spyne.RoomData

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spyne.RoomData.PrefUtils.getVersionCode
import com.example.spyne.RoomData.PrefUtils.removeVersionCode
import com.example.spyne.RoomData.PrefUtils.storeVersionCode

@Database(entities = [PictureModel::class], version = AppDatabase.v, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImagesDao?

    companion object {
        private val LOG_TAG = AppDatabase::class.java.simpleName
        private val LOCK = Any()
        const val v = 1
        var changed = false
        private const val DATABASE_NAME = "imageslist"
        private var sInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {
            if (sInstance == null) {
                if (!getVersionCode(context, "version")!!.isEmpty()) {
                    if (getVersionCode(context, "version")!!.toInt() != v) {
                        changed = true
                        removeVersionCode(context, "version")
                        storeVersionCode(context, v.toString(), "version")
                    } else {
                        changed = false
                    }
                } else {
                    storeVersionCode(context, v.toString(), "version")
                }

                synchronized(LOCK) {
                    Log.d(LOG_TAG, "Creating new database instance")
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, DATABASE_NAME
                    )
                        .fallbackToDestructiveMigration()
                        .addCallback(roomCallback)
                        .build()
                }
            }

            Log.d(LOG_TAG, "Getting the database instance")
            return sInstance
        }

        private val roomCallback: Callback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
            }
        }
    }
}