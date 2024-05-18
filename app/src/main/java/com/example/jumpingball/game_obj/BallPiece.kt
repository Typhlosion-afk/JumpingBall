package com.example.jumpingball.game_obj

import com.example.jumpingball.util.Converter.toDp
import com.example.jumpingball.util.Converter.toRadian
import com.example.jumpingball.util.LogUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class BallPiece(
    private var angle: Int = 0,
    var x: Float = 0f,
    var y: Float = 0f,
    var radius: Float = Bird.Util.viewRadius,
    private var moveSpeed: Float = 5f.toDp(),
    private val smallSpeed: Float = 0.7f.toDp()
) {
    fun nextPos() {
        x += (cos(angle.toRadian()) * moveSpeed).toFloat()
        y += (sin(angle.toRadian()) * moveSpeed).toFloat()
        if(radius > 0){
            radius -= smallSpeed
        }
    }

    fun randomNewAngle(){
        angle = Random.nextInt(0, 360)
    }

    fun randomNewSpeed(){
        moveSpeed = Random.nextInt(4,7).toFloat().toDp()
    }
}