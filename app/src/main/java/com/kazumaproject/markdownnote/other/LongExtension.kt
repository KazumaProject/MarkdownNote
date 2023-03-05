package com.kazumaproject.markdownnote.other

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun Long.convertLongToTime(): String {
    val date = Date(this)
    val format = SimpleDateFormat("yyyy/MM/dd")
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun Long.convertLongToTimeTwitterFormat(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd MMM")
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun Long.convertLongToTimeTwitterFormatInJapanese(): String {
    val date = Date(this)
    val format = SimpleDateFormat("MMMdd")
    return format.format(date)
}

@SuppressLint("SimpleDateFormat")
fun String.convertDateToLong(): Long? {
    val df = SimpleDateFormat("yyyy/MM/dd")
    return df.parse(this)?.time
}