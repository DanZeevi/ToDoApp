package com.zdan.todoapp.util

import android.icu.text.DateFormat.getDateInstance
import android.icu.util.Calendar

class Util {
}

fun Long.toDateString(): String {
    val dateFormat = getDateInstance()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return dateFormat.format(calendar.time)
}