package com.example.spyne.RoomData


import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "images")
class PictureModel {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var name: String
    var status: String
    var imageUrl: String
    var imageUri: String
    var selecteddate: Date
    var resultdate: Date

    constructor() : this(0, "", "", Date(), "", "", Date())

    @Ignore
    constructor(
        name: String,imageUri: String,
        selecteddate: Date, imageUrl: String, status: String, resultdate: Date
    ) {
        this.name = name
        this.imageUri = imageUri
        this.selecteddate = selecteddate
        this.status = status
        this.imageUrl = imageUrl
        this.resultdate = resultdate
    }

    constructor(
        id: Int,
        name: String,
        imageUri: String,
        selecteddate: Date,
        status: String,
        imageUrl: String,
        resultdate: Date
    ) {
        this.id = id
        this.name = name
        this.imageUri = imageUri
        this.selecteddate = selecteddate
        this.status = status
        this.imageUrl = imageUrl
        this.resultdate = resultdate
    }
}