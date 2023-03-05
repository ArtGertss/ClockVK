package com.example.clockvk

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class ViewClock(context: Context, attributeSet: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attributeSet, defStyleAttr) {

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private val mClockHours = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

    private var mRadius: Int = 0

    private var mPaint: Paint = Paint()

    private var isInitialized: Boolean = false

    private var defaultMargin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics)
    private var textFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18f, resources.displayMetrics)
    private val handSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics)

    private var lightBackgroundColor = ContextCompat.getColor(context, R.color.defaultLightBackgroundColor)
    private var lightShadowColor = ContextCompat.getColor(context, R.color.defaultLightShadowColor)
    private var darkShadowColor = ContextCompat.getColor(context, R.color.defaultDarkShadowColor)
    private var borderColor = ContextCompat.getColor(context, R.color.defaultBorderColor)
    private var minHourHandsColor = ContextCompat.getColor(context, R.color.defaultMinHourHandsColor)
    private var secondsHandColor = ContextCompat.getColor(context, R.color.defaultSecondsHandsColor)
     var textColor = ContextCompat.getColor(context, R.color.defaultTextColor)

    private val mRect = Rect()

    init {
        setUpAttributes(attributeSet)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isInitialized) initialize()

        canvas.scale(0.9f, 0.9f, (mWidth / 2).toFloat(), (mHeight / 2).toFloat())

        drawClockShape(canvas)

        drawNumerals(canvas)

        drawHands(canvas)

        drawCenterCircle(canvas)

    }

    fun setUpAttributes(attributes: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(attributes, R.styleable.ViewClock, 0, 0)

        lightBackgroundColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_lightBackgroundColor, R.color.defaultLightBackgroundColor))
        lightShadowColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_lightShadowColor, R.color.defaultLightShadowColor))
        darkShadowColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_darkShadowColor, R.color.defaultDarkShadowColor))
        borderColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_borderColor, R.color.defaultBorderColor))
        minHourHandsColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_minHourHandsColor, R.color.defaultMinHourHandsColor))
        secondsHandColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_secondsHandColor, R.color.defaultSecondsHandsColor))
        textColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_textColor, R.color.defaultTextColor))
        textFontSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, typedArray.getDimension(R.styleable.ViewClock_textSize, textFontSize), context.resources.displayMetrics)
        typedArray.recycle()
    }

    private fun initialize() {
        mHeight = height
        mWidth = width

        val minHeightWidthValue = min(mHeight, mWidth)
        mRadius = (minHeightWidthValue / 2 - defaultMargin).toInt()

        mPaint.isAntiAlias = true

        isInitialized = true
    }

    private fun drawClockShape(canvas: Canvas) {
        mPaint.strokeWidth = 6f
        mPaint.style = Paint.Style.STROKE
        mPaint.color = borderColor
        canvas.drawCircle((mWidth / 2).toFloat(), (mHeight / 2).toFloat(), (mRadius + 50).toFloat(), mPaint)

        mPaint.reset()
    }


    private fun drawNumerals(canvas: Canvas) {

        mPaint.textSize = textFontSize
        mPaint.isFakeBoldText = true
        mPaint.color = textColor

        for (hour in mClockHours) {
            val tmp = hour.toString()

            mPaint.getTextBounds(tmp, 0, tmp.length, mRect)
            val angle = Math.PI / 6 * (hour - 3)
            val x = (mWidth / 2 + cos(angle) * mRadius - mRect.width() / 2).toFloat()
            val y = ((mHeight / 2).toDouble() + sin(angle) * mRadius + (mRect.height() / 2)).toFloat()


            canvas.drawText(tmp, x, y, mPaint)

        }
    }

    private fun drawHands(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)

        drawHandLine(canvas, (hour + calendar.get(Calendar.MINUTE) / 60f) * 5f, HandType.HOUR)
        drawHandLine(canvas, calendar.get(Calendar.MINUTE).toFloat(), HandType.MINUTE)
        drawHandLine(canvas, calendar.get(Calendar.SECOND).toFloat(), HandType.SECONDS)

        postInvalidateDelayed(500)
        invalidate()

        mPaint.reset()
    }

    private fun drawHandLine(canvas: Canvas, value: Float, handType: HandType) {
        val angle = Math.PI * value / 30 - Math.PI / 2

        val handRadius = when (handType) {
            HandType.HOUR -> mRadius - mRadius / 3
            HandType.MINUTE -> mRadius - mRadius / 6
            HandType.SECONDS -> mRadius - mRadius / 9
        }

        mPaint.color = if (handType == HandType.SECONDS) secondsHandColor else minHourHandsColor
        mPaint.strokeWidth = if (handType == HandType.SECONDS) handSize else handSize * 2
        mPaint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            (mWidth / 2 + cos(angle) * handRadius).toFloat(),
            (mHeight / 2 + sin(angle) * handRadius).toFloat(),
            mPaint
        )

    }

    private fun drawCenterCircle(canvas: Canvas) {
        mPaint.color = secondsHandColor
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, handSize+6, mPaint)
    }

  /*  fun setTextcolor(@ColorInt textcolor: Int) {
        textColor = textcolor
    }
    *//*fun setTextColor(@ColorInt newTextColor: Int){
        textColor = ContextCompat.getColor(context, typedArray.getResourceId(R.styleable.ViewClock_textColor, R.color.defaultTextColor))
    }*/

    private enum class HandType { HOUR, MINUTE, SECONDS }

}