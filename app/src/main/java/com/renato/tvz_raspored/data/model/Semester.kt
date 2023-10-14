package com.renato.tvz_raspored.data.model

data class Semester(val SemesterNumber: String, val Department: String) {
    override fun toString(): String {
        return "Semester(semesterNumber='$SemesterNumber', department='$Department')"
    }
}