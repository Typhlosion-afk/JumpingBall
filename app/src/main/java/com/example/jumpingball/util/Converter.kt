package com.example.jumpingball.util

import com.example.jumpingball.App

object Converter {
    fun Float.toDp(): Float = this * App.instances.resources.displayMetrics.density
    fun Int.toRadian(): Double = this * 0.0174
}