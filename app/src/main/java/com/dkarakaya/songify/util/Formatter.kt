package com.dkarakaya.songify.util

fun formatDuration(duration: String): String {
    val d = duration.toInt() / 1000
    val min = d / 60
    val sec = d % 60
    return String.format("%02d:%02d", min, sec)
}

fun formatTitle(array: Array<String>): Array<String> {
    array[1].trim { it <= ' ' }
    array[0].trim { it <= ' ' }
    return array
}
