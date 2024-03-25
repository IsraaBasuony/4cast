package com.iti.a4cast.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity("fav_locations")
data class FavLocation (
    @PrimaryKey @ColumnInfo(name = "id")
    var id: String = UUID.randomUUID().toString(),
    var latitude: Double,
    var longitude:Double
    )