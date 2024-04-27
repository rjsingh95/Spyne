package com.example.spyne.RoomData

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import java.util.Date

@Dao
interface ImagesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(group: PictureModel)

    @Update
    fun update(group: PictureModel)

    @Delete
    fun delete(group: PictureModel)

    @Query("SELECT * FROM images ORDER BY id")
    fun loadAllImages(): List<PictureModel>?

    @Query("SELECT * FROM images WHERE id = :id")
    fun loadImageById(id: Int): PictureModel

    @Query("SELECT * FROM images WHERE imageUri = :VideoUri")
    fun loadImageByuri(VideoUri: String?): PictureModel

    @Query("SELECT * FROM images WHERE resultdate = :resultdate")
    fun loadImageByTime(resultdate: Date?): PictureModel
}