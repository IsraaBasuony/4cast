package com.iti.a4cast.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "AlertModel")
data class AlertModel(
    @PrimaryKey @ColumnInfo(name = "id")
    var id: String = UUID.randomUUID().toString(),
    val longitude: Double,
    val latitude: Double,
    val start: Long,
    val end: Long,
    val type: String
)

