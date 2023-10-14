package com.renato.tvz_raspored.data.model

data class Department(val Code: String, val Name: String) {
    override fun toString(): String = Name
}