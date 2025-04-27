package eki.dore.flabbygame.util

import eki.dore.flabbygame.App

object Converter {
    fun Float.toDp(): Float = this * App.instances.resources.displayMetrics.density
    fun Int.toRadian(): Double = this * 0.0174
}