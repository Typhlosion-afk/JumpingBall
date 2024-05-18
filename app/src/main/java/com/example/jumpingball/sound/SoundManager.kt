package com.example.jumpingball.sound

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.jumpingball.R
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
class SoundManager(private val soundPool: SoundPool, private val context: Context) {
    private val soundTrack: SoundTrack = SoundTrack()
    private var mediaTheme: MediaPlayer? = null
    private var isSoundOff = false
    private var isMusicOff = false

    init {
        GlobalScope.launch {
            soundTrack.touch = soundPool.load(context, R.raw.touch_sound, 1)
            soundTrack.die = soundPool.load(context, R.raw.sound_die, 1)
            soundTrack.point = soundPool.load(context, R.raw.sound_get_point, 1)

            mediaTheme = MediaPlayer.create(context, R.raw.them_sound)
            mediaTheme?.isLooping = true
        }
    }

    fun setSoundOff(soundOff: Boolean) {
        isSoundOff = soundOff
    }

    fun setMusicOff(musicOff: Boolean) {
        isMusicOff = musicOff
    }

    fun playThemeSound() {
        mediaTheme?.let {
            if (!it.isPlaying && !isMusicOff) {
                it.start()
            }
        }
    }

    /**
     * => soundID id of audio file returned from method load()
     * => leftVolume volume (range = 0.0 to 1.0)
     * => rightVolume volume (range = 0.0 to 1.0)
     * => priority (0 = lowest priority)
     * => loop (0 = no loop, -1 = loop forever)
     * => rate speedup (1.0 = normal playback, range 0.5 to 2.0)
     */
    fun playTouchSound() {
        if (!isSoundOff) {
            soundTrack.touch?.let {
                GlobalScope.launch {
                    soundPool.play(it, 1f, 1f, 1, 0, 1f)
                }
            }
        }
    }

    fun playDieSound() {
        if (!isSoundOff) {
            soundTrack.die?.let {
                GlobalScope.launch {
                    soundPool.play(it, 0.8f, 0.8f, 1, 0, 1f)
                }
            }
        }
    }

    fun playGetPointSound() {
        if (!isSoundOff) {
            soundTrack.point?.let {
                GlobalScope.launch {
                    soundPool.play(it, 0.6f, 0.6f, 1, 0, 1f)
                }
            }
        }
    }

    fun stopTheme() {
        mediaTheme?.stop()
    }

    fun releaseAll() {
        mediaTheme?.release()
        mediaTheme = null
    }
}