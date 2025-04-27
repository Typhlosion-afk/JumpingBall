package eki.dore.flabbygame.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import eki.dore.flabbygame.R
import eki.dore.flabbygame.game_obj.BallPiece
import eki.dore.flabbygame.game_obj.ColorSet
import eki.dore.flabbygame.game_obj.Bird
import eki.dore.flabbygame.game_obj.Column
import eki.dore.flabbygame.util.Converter.toDp

class GameSurfaceView(context: Context?, attrs: AttributeSet) : SurfaceView(context, attrs) {

    private val textScoreSize = 40f.toDp()
    private val defaultTopScoreSpace = 60f.toDp()

    @ColorInt
    private var colorBird: Int = 0

    @ColorInt
    private var colorColumn: Int = 0

    @ColorInt
    private var colorBackground: Int = 0

    @ColorInt
    private var colorShadow: Int = 0

    private var onTapEvent: OnTapEvent? = null

    private lateinit var bird: Bird
    private var birdPaint = Paint()
    private var columnPaint = Paint()
    private var scorePaint = Paint()
    private var scoreBgPaint = Paint()

    private var listColumnRectF = emptyList<RectF>()

    private var viewHeight: Int = 0
    private var viewWidth: Int = 0

    private var column = Column(500f, 500f)

    private var birdViewRadius = 0f;
    private var curScore = 0

    private var piecesPath = Path()

    private var isDying = false
    private var isAlive = false
    private var pieces: List<BallPiece> = emptyList()
    private var isUnDeath = false

    init {
        val a = context?.obtainStyledAttributes(attrs, R.styleable.GameView)
        colorBird =
            a?.getColor(R.styleable.GameView_bird_color, Color.BLUE) ?: Color.BLUE
        colorColumn =
            a?.getColor(R.styleable.GameView_column_color, Color.BLUE) ?: Color.BLUE
        colorBackground =
            a?.getColor(R.styleable.GameView_background_color, Color.WHITE) ?: Color.WHITE

        initBird()
        initPaint()
        a?.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        viewHeight = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        viewWidth = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)

        setMeasuredDimension(viewWidth, viewHeight)
    }

    fun render(canvas: Canvas?) {
        canvas?.let {
            drawBackground(it)
            if (isAlive) {
                drawBird(it)
            }
            drawListColumn(it)
            drawScore(it)
            if (isDying) {
                drawPiece(it)
            }
        }
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(colorBackground)
    }

    private fun initBird() {
        bird = Bird(cy = viewHeight / 2f)
        birdViewRadius = Bird.Util.viewRadius
    }

    private fun initPaint() {
        with(birdPaint) {
            style = Paint.Style.FILL
            color = colorBird
        }

        with(columnPaint) {
            style = Paint.Style.FILL
            color = colorColumn
        }

        with(scoreBgPaint) {
            color = colorBackground
            style = Paint.Style.FILL
            isAntiAlias = true
        }


        val customTypeface = ResourcesCompat.getFont(context, R.font.nunito_bold)
        with(scorePaint) {
            style = Paint.Style.FILL
            color = colorBird
            isAntiAlias = true
            textSize = textScoreSize
            typeface = Typeface.create(customTypeface, Typeface.BOLD)
        }
    }

    private fun drawBird(canvas: Canvas) {
        canvas.drawCircle(bird.cx, bird.cy, birdViewRadius, birdPaint)
    }

    private fun drawListColumn(canvas: Canvas) {
        if (listColumnRectF.isNotEmpty()) {
            listColumnRectF.forEach {
                canvas.drawRect(it, columnPaint)
            }
        }
    }

    private fun drawPiece(canvas: Canvas) {
        piecesPath.reset()
        pieces.forEach {
            if (it.radius > 0) {
                piecesPath.addCircle(
                    it.x,
                    it.y,
                    it.radius,
                    Path.Direction.CW
                )
                it.nextPos()
            } else {
                isDying = false
            }
        }
        piecesPath.close()
        canvas.drawPath(piecesPath, birdPaint)
    }

    private fun drawScore(canvas: Canvas) {
        val bounds = Rect()
        val str = curScore.toString()
        scorePaint.getTextBounds(str, 0, str.length, bounds)
        canvas.drawText(
            str,
            viewWidth / 2f - bounds.width() / 2f,
            defaultTopScoreSpace,
            scorePaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                onTapEvent?.onTap()
            }
        }
        return true
    }

    fun setBirdY(newCy: Float) {
        bird.cy = newCy
    }

    fun setScore(score: Int) {
        curScore = score
    }

    fun setBirdX(newCx: Float) {
        bird.cx = newCx
    }

    fun setListColumn(newList: List<RectF>) {
        listColumnRectF = newList
    }

    fun setBirdIsAlive(isAlive: Boolean) {
        this.isAlive = isAlive
    }

    fun setColor(@ColorInt bird: Int, @ColorInt column: Int, @ColorInt background: Int) {
        colorBird = bird
        colorColumn = column
        colorBackground = background
        initPaint()
    }

    fun setColor(colorSet: ColorSet) {
        colorBird = colorSet.bird
        colorColumn = colorSet.column
        colorBackground = colorSet.background
        initPaint()
    }


    fun setTapAction(event: OnTapEvent) {
        onTapEvent = event
    }

    fun setPieces(pieces: List<BallPiece>) {
        isDying = true
        this.pieces = pieces
    }

    class OnTapEvent(val clickListener: () -> Unit) {
        fun onTap() = clickListener()
    }
}