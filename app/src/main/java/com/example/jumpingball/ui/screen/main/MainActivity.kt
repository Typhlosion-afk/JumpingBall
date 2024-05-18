package com.example.jumpingball.ui.screen.main

import android.graphics.Canvas
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.example.jumpingball.R
import com.example.jumpingball.customview.GameSurfaceView
import com.example.jumpingball.databinding.ActivityMainBinding
import com.example.jumpingball.game_obj.BallPiece
import com.example.jumpingball.game_obj.Bird
import com.example.jumpingball.game_obj.ColorSet
import com.example.jumpingball.game_obj.Column
import com.example.jumpingball.sound.SoundManager
import com.example.jumpingball.sound.SoundTrack
import com.example.jumpingball.ui.screen.start.StartActivity.Companion.KEY_COLOR_SET
import com.example.jumpingball.ui.screen.start.StartViewModel.Companion.MUSIC_KEY
import com.example.jumpingball.ui.screen.start.StartViewModel.Companion.SOUND_KEY
import com.example.jumpingball.ui.pause_screen.PauseFragment
import com.example.jumpingball.ui.screen.game_over.GameOverFragment
import com.example.jumpingball.util.Constant.BEST_SCORE
import com.example.jumpingball.util.GameValue
import com.example.jumpingball.util.LogUtils
import com.example.jumpingball.util.SharedPrefs
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext

@OptIn(DelicateCoroutinesApi::class)
class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private var bird = Bird()
    private var screenHeight: Int = 0
    private var screenWidth: Int = 0

    private var dangerColumn: Column? = null

    private var frameFallingCounter = 0

    private var uiJob: Job? = null
    private var calculatorJob: Job? = null

    private val spaceHeight: Float = Column.Util.spaceHeight
    private val columnDistance: Float = Column.Util.distanceBetweenColumns

    private val listCol = ArrayList<Column>()

    private var isRunning = false
    private var isAlive = true
    private var isPausing = false
    private var colorSet: ColorSet? = null

    private lateinit var gameOverFragment: GameOverFragment
    private lateinit var pauseFragment: PauseFragment
    private lateinit var soundManager: SoundManager
    private lateinit var soundPool: SoundPool

    private var userPoint: Int = 0

    private var isSoundOff = true
    private var isMusicOff = true

    private val pieces = ArrayList<BallPiece>()
    private var isUnDeath = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels

        initData()
        initSoundPool()
        initView()
        initGameView()
        setUpStartState()
        initFragment()
    }

    private fun initData() {
        colorSet = intent.getParcelableExtra(KEY_COLOR_SET)
        isMusicOff = intent.getBooleanExtra(MUSIC_KEY, true)
        isSoundOff = intent.getBooleanExtra(SOUND_KEY, true)
        for (i in 0..7) {
            pieces.add(BallPiece())
        }
    }

    private fun initGameView() {
        with(binding.gameView) {
            holder.addCallback(this@MainActivity)
            setTapAction(GameSurfaceView.OnTapEvent {
                if (!isRunning && isAlive && !isPausing) {
                    isRunning = true
                }
                if (!isPausing) {
                    frameFallingCounter = 0
                }
                soundManager.playTouchSound()
            })
            if (colorSet != null) {
                setColor(colorSet!!)
            } else {
                setColor(
                    bird = getColor(R.color.bird),
                    column = getColor(R.color.column),
                    background = getColor(R.color.background)
                )
            }
        }
    }

    private fun initView() {
        binding.txtCountDown.visibility = View.INVISIBLE
    }

    private fun initFragment() {
        gameOverFragment = GameOverFragment()
        gameOverFragment.setClickEvent(GameOverFragment.EventClick {
            supportFragmentManager.beginTransaction().remove(gameOverFragment).commit()
            newGame()
        })

        pauseFragment = PauseFragment()
        pauseFragment.setClickEvent(object : PauseFragment.PauseScreenEvent {
            override fun onHomeClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                this@MainActivity.onBackPressed()
            }

            override fun onNewGameClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                restartGame()
            }

            override fun onContinueClick() {
                supportFragmentManager.beginTransaction().remove(pauseFragment).commit()
                resume()
            }
        })
    }

    private fun setUpStartState() {
        setUpStartScore()
        setBirdStartState()
        initListColumn()
    }

    private fun setUpStartScore() {
        userPoint = 0
        binding.gameView.setScore(userPoint)
    }

    private fun initListColumn() {
        listCol.clear()
        for (i in 0 until Column.Util.columnsSize) {
            listCol.add(
                Column(
                    spaceX = screenWidth + i * columnDistance,
                    spaceY = randomSpaceY()
                )
            )
        }
        val listRectF = ArrayList<RectF>()
        listCol.forEach { c ->
            if (c.spaceX - Column.Util.width / 2f <= screenWidth) {
                listRectF.add(
                    RectF(
                        c.spaceX - c.width / 2f,
                        0f,
                        c.spaceX + c.width / 2f,
                        c.spaceY - c.spaceHeight / 2f
                    )
                )
                listRectF.add(
                    RectF(
                        c.spaceX - c.width / 2f,
                        c.spaceY + c.spaceHeight / 2f,
                        c.spaceX + c.width / 2f,
                        screenHeight.toFloat(),
                    )
                )
            }
        }
        binding.gameView.setListColumn(listRectF)
    }

    private fun randomSpaceY() = Column.Util.randomSpaceY(
        screenHeight = screenHeight,
        spaceHeight = spaceHeight,
        minHeight = Column.Util.columnMinHeight
    )

    private fun setBirdStartState() {
        bird.cy = screenHeight / 2f
        bird.cx = screenWidth * 15f / 100f

        with(binding.gameView) {
            setBirdIsAlive(true)
            setBirdY(bird.cy)
            setBirdX(bird.cx)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun initJob() {
        calculatorJob = lifecycleScope.launch(Dispatchers.Default) {
            while (isActive) {
                val startCalculatorLoopTime = SystemClock.elapsedRealtime()
                /**
                 * Event bird touch to limit of screen
                 */
                if (bird.cy + bird.radius > screenHeight || bird.cy - bird.radius < 0) {
                    setDieEvent()
                }

                /**
                 * Calculator new bird's position after each delta time (@deltaT)
                 */
                if (isRunning) {
                    with(GameValue) {
                        val t = frameFallingCounter * deltaT
                        bird.cy += deltaT * acceleration * (t - 0.5f * deltaT) - bird.jumpPower * deltaT
                        binding.gameView.setBirdY(bird.cy)
                    }
                    frameFallingCounter++
                }

                /**
                 * Check event bird touch to column
                 */
                val col = listCol[0]
                if (col.spaceX - col.width / 2f <= bird.cx + bird.radius
                    && col.spaceX + col.width / 2f >= bird.cx - bird.radius
                ) {
                    if (col.spaceY - col.spaceHeight / 2f >= bird.cy - bird.radius
                        || col.spaceY + col.spaceHeight / 2f <= bird.cy + bird.radius
                    ) {
                        setDieEvent()
                    }
                }

                /**
                 * Check event plus point
                 */
                if (col !== dangerColumn) {
                    if (col.spaceX + col.width / 2f < bird.cx - bird.radius) {
                        binding.gameView.setScore(++userPoint)
                        soundManager.playGetPointSound()
                        dangerColumn = col
                    }
                }

                /**
                 * Add new Column come and remove Column was out of screen
                 */
                if (isRunning && listCol.isNotEmpty()) {
                    if (listCol[0].spaceX + listCol[0].width / 2f <= 0) {
                        listCol.add(
                            Column(
                                spaceX = listCol[listCol.size - 1].spaceX + columnDistance,
                                spaceY = randomSpaceY()
                            )
                        )
                        listCol.removeAt(0)
                    }
                    val listRectF = ArrayList<RectF>()
                    listCol.forEach { c ->
                        c.spaceX -= c.moveSpeed * GameValue.deltaT
                        if (c.spaceX - Column.Util.width / 2f <= screenWidth) {
                            listRectF.add(
                                RectF(
                                    c.spaceX - c.width / 2f,
                                    0f,
                                    c.spaceX + c.width / 2f,
                                    c.spaceY - c.spaceHeight / 2f
                                )
                            )
                            listRectF.add(
                                RectF(
                                    c.spaceX - c.width / 2f,
                                    c.spaceY + c.spaceHeight / 2f,
                                    c.spaceX + c.width / 2f,
                                    screenHeight.toFloat(),
                                )
                            )
                        }
                    }
                    binding.gameView.setListColumn(listRectF)
                }

                /**
                 * Delay per loop
                 */
                val endCalculatorLoopTime = SystemClock.elapsedRealtime()
                val sleepTime =
                    GameValue.deltaTInCal - (endCalculatorLoopTime - startCalculatorLoopTime)
                delay(sleepTime)
            }
        }

        uiJob = lifecycleScope.launch(newSingleThreadContext("RenderThread")) {
            var canvas: Canvas?
            while (isActive) {
                canvas = null
                with(binding.gameView) {
                    try {
                        synchronized(holder) {
                            canvas = holder.lockCanvas()
                            render(canvas)
                        }
                    } finally {
                        if (canvas != null) {
                            holder.unlockCanvasAndPost(canvas)
                        }
                    }
                }
            }
        }
    }

    private fun setDieEvent() {
        if (!isUnDeath) {
            isRunning = false
            isAlive = false

            soundManager.playDieSound()

            pieces.forEach {
                it.x = bird.cx
                it.y = bird.cy
                it.radius = Bird.Util.viewRadius
                it.randomNewAngle()
                it.randomNewSpeed()
            }

            binding.gameView.setPieces(pieces)
            binding.gameView.setBirdIsAlive(false)
            gameOverFragment.arguments = bundleOf(KEY_SCORE to userPoint)
            supportFragmentManager.beginTransaction()
                .replace(binding.overlayView.id, gameOverFragment, null)
                .commit()

            calculatorJob?.cancel()

            calculatorJob = null

            if ((SharedPrefs[BEST_SCORE, Int::class.java] ?: 0) < userPoint) {
                SharedPrefs.put(BEST_SCORE, userPoint)
            }
        }
    }

    private fun restartGame() {
        isRunning = false
        isAlive = false
        calculatorJob?.cancel()

        calculatorJob = null

        if ((SharedPrefs[BEST_SCORE, Int::class.java] ?: 0) < userPoint) {
            SharedPrefs.put(BEST_SCORE, userPoint)
        }
        newGame()
    }

    private fun newGame() {
        setUpStartState()
        isRunning = false
        isPausing = false
        isAlive = true
        initJob()
    }

    private fun pause() {
        isRunning = false
        isPausing = true
        supportFragmentManager.beginTransaction()
            .add(binding.overlayView.id, pauseFragment, null)
            .commit()
        LogUtils.d("pause")
    }

    private fun resume() {
        lifecycleScope.launch {
            binding.txtCountDown.visibility = View.VISIBLE
            binding.txtCountDown.setTextColor(colorSet!!.bird)
            for (i in 3 downTo 1) {
                binding.txtCountDown.text = i.toString()
                delay(1000L)
            }
            isRunning = true
            isPausing = false

            binding.txtCountDown.visibility = View.INVISIBLE
            LogUtils.d("resume")
        }
    }

    private fun initSoundPool() {
        val attrsSound = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool
            .Builder()
            .setMaxStreams(3)
            .setAudioAttributes(attrsSound)
            .build()

        soundManager = SoundManager(soundPool, this)
        soundManager.setSoundOff(isSoundOff)
        soundManager.setMusicOff(isMusicOff)
    }

    override fun onPause() {
        if (isRunning) {
            pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        binding.gameView.visibility = View.GONE
        soundPool.release()
        soundManager.releaseAll()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (isRunning) {
            pause()
        } else {
            super.onBackPressed()
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        LogUtils.d("Create surface")
        initJob()
        soundManager.playThemeSound()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        calculatorJob?.cancel()
        uiJob?.cancel()

        calculatorJob = null
        uiJob = null
        soundManager.stopTheme()
    }

    companion object {
        const val KEY_SCORE = "key_score"
    }
}