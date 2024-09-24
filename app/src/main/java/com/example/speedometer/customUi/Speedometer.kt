package com.example.speedometer.customUi

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.speedometer.R
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class Speedometer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleRes) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arcWidth = context.resources.getDimension(R.dimen.dp_16)
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var initialiseNobs = true

    private var isDraggingUpperCircle = false
    private var isDraggingLowerCircle = false

    private val lowerCircleCenter = Point(0f, 0f)
    private val upperCircleCenter = Point(0f, 0f)

    private var lowerNobCenter = Point(0f, 0f)
    private var upperNobCenter = Point(0f, 0f)

    private var radius = 0f


    init {
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeWidth = arcWidth
        arcPaint.color = context.getColor(R.color.green_100)
        circlePaint.color = context.getColor(R.color.white)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        lowerCircleCenter.x = width.toFloat() / 2f
        lowerCircleCenter.y = height.toFloat()

        upperCircleCenter.x = width.toFloat() / 2f
        upperCircleCenter.y = 0f

        radius = min(lowerCircleCenter.x, lowerCircleCenter.y) - 150f

        if (initialiseNobs) {
            upperNobCenter = getNewCenter(radius, (170 * PI / 180).toFloat(), upperCircleCenter)
            lowerNobCenter = getNewCenter(radius, (-170 * PI / 180).toFloat(), lowerCircleCenter)

            initialiseNobs = false
        }

        canvas.drawCircle(lowerCircleCenter.x, lowerCircleCenter.y, radius, arcPaint)
        canvas.drawCircle(upperCircleCenter.x, upperCircleCenter.y, radius, arcPaint)

        canvas.drawCircle(upperNobCenter.x, upperNobCenter.y, 50f, circlePaint)
        canvas.drawCircle(lowerNobCenter.x, lowerNobCenter.y, 50f, circlePaint)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                isDraggingUpperCircle = false
                isDraggingLowerCircle = false
                if (isDraggable(event, lowerCircleCenter, radius = radius)) {
                    isDraggingLowerCircle = true
                } else if (isDraggable(event, upperCircleCenter, radius = radius)) {
                    isDraggingUpperCircle = true
                }
            }

            MotionEvent.ACTION_MOVE -> {
                if (isDraggingLowerCircle) {
                    val angle = getAngle(Point(event.x, event.y), lowerCircleCenter)
                    if ((angle * 180 / PI).toInt() in -170..-10) {
                        lowerNobCenter = getNewCenter(
                            radius,
                            angle,
                            lowerCircleCenter
                        )
                        invalidate()
                    }
                } else if (isDraggingUpperCircle) {
                    val angle = getAngle(Point(event.x, event.y), upperCircleCenter)
                    if ((angle * 180 / PI).toInt() in 10..170) {
                        upperNobCenter = getNewCenter(
                            radius,
                            angle,
                            upperCircleCenter
                        )
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                isDraggingUpperCircle = false
                isDraggingLowerCircle = false
            }
        }
        return true
    }

    private fun getAngle(point: Point, center: Point) = atan2((point.y - center.y), (point.x - center.x))

    private fun isDraggable(event: MotionEvent, center: Point, radius: Float) =
        sqrt(
            (event.x - center.x).pow(2) + (event.y - center.y).pow(2).toDouble()
        ) <= radius + arcWidth

    private fun getNewCenter(radius: Float, angle: Float, center: Point) =
        Point(center.x + radius * cos(angle), center.y + radius * sin(angle))

}

data class Point(var x: Float, var y: Float)