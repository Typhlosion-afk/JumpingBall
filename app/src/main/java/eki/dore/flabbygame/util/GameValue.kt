package eki.dore.flabbygame.util

import eki.dore.flabbygame.util.Converter.toDp

object GameValue {
    const val fps = 60f
    const val deltaTInCal: Long = 9L
    const val deltaT: Float = 9f / 1000f
    val acceleration: Float = 2100f.toDp()
}