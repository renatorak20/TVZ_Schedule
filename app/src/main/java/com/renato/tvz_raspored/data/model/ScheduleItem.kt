package com.renato.tvz_raspored.data.model

data class CourseInfo(
    val title: String,
    val lecturer: String,
    val classRoom: String,
    val direction: String,
    val start: String,
    val end: String
)