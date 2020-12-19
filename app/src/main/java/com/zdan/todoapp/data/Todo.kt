package com.zdan.todoapp.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Todo(
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0,
    var description: String,
    @ColumnInfo(name = "is_completed")
    var isCompleted: Boolean = false,
    @ColumnInfo(name = "is_important")
    var isImportant: Boolean = false,
    @ColumnInfo(name = "date_created")
    val dateCreated : Long = System.currentTimeMillis()
)
