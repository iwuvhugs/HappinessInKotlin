package com.iwuvhugs.happinessinkotlin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.support.v4.view.MotionEventCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.os.Parcel
import android.os.Parcelable

class FaceView : View {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)

    private var faceDrawer = FaceDrawer()

    private var happiness = 100f

    private var centerX = 0f
    private var centerY = 0f
    private var y1 = 0f
    private var faceRadius = 0f

    private val HAPPINESS_GESTURE_SCALE = 4f
    private val SCALE = 0.9f
    private val FACE_RADIUS_TO_EYE_RADIUS_RATIO = 10f
    private val FACE_RADIUS_TO_EYE_OFFSET_RATIO = 3f
    private val FACE_RADIUS_TO_EYE_SEPARATION_RATIO = 1.5f
    private val FACE_RADIUS_TO_MOUTH_WIDTH_RATIO = 1f
    private val FACE_RADIUS_TO_MOUTH_HEIGHT_RATIO = 3f
    private val FACE_RADIUS_TO_MOUTH_OFFSET_RATIO = 3f

    enum class Eye {
        LEFT,
        RIGHT
    }

    override fun onDraw(canvas: Canvas) {
        faceDrawer.drawFace(canvas)
    }

    override fun onSaveInstanceState(): Parcelable {
        val parcelable = super.onSaveInstanceState()
        val faceState = FaceState(parcelable)
        faceState.state = happiness
        return faceState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val faceState = state as FaceState
        super.onRestoreInstanceState(faceState.superState)
        setHappiness(faceState.state)
    }

    fun setHappiness(happiness: Float) {
        this.happiness = happiness
        postInvalidate()
    }

    private fun changeHappiness(happinessChange: Float) {
        happiness = Math.min(Math.max(happiness + happinessChange, 0f), 100f)
        postInvalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        faceRadius = Math.min(w, h) / 2 * SCALE
        centerX = (w / 2).toFloat()
        centerY = (h / 2).toFloat()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(event)
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                y1 = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = -(event.y - y1) / HAPPINESS_GESTURE_SCALE
                y1 = event.y
                if (deltaY != 0f) {
                    changeHappiness(deltaY)
                }
                return true
            }
            else -> return super.onTouchEvent(event)
        }
    }

    inner class FaceDrawer {

        private var paint: Paint = Paint()

        init {
            paint.color = Color.BLUE
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 20f
        }

        fun drawFace(canvas: Canvas) {
            canvas.save()
            canvas.translate(centerX, centerY)

            drawFaceOval(canvas)
            drawEye(canvas, Eye.LEFT)
            drawEye(canvas, Eye.RIGHT)
            drawMouth(canvas)

            canvas.restore()
            canvas.save()
        }

        private fun drawFaceOval(canvas: Canvas) {
            canvas.drawCircle(0f, 0f, faceRadius, paint)
        }

        private fun drawEye(canvas: Canvas, eye: Eye) {
            val eyeRadius = faceRadius / FACE_RADIUS_TO_EYE_RADIUS_RATIO
            val eyeVerticalOffset = faceRadius / FACE_RADIUS_TO_EYE_OFFSET_RATIO
            val eyeHorizontalSeparation = faceRadius / FACE_RADIUS_TO_EYE_SEPARATION_RATIO

            val eyeY = -eyeVerticalOffset
            val eyeX = (if (eye === Eye.LEFT) -1 else 1) * eyeHorizontalSeparation / 2

            canvas.drawCircle(eyeX, eyeY, eyeRadius, paint)
        }

        private fun drawMouth(canvas: Canvas) {
            val mouthWidth = faceRadius / FACE_RADIUS_TO_MOUTH_WIDTH_RATIO
            val mouthHeight = faceRadius / FACE_RADIUS_TO_MOUTH_HEIGHT_RATIO
            val mouthVerticalOffset = faceRadius / FACE_RADIUS_TO_MOUTH_OFFSET_RATIO

            val fractionOfMaxSmile = (happiness - 50) / 50
            val smileHeight = Math.max(Math.min(fractionOfMaxSmile, 1f), -1f) * mouthHeight

            val path = Path()
            path.moveTo(-mouthWidth / 2, mouthVerticalOffset)
            path.cubicTo(-mouthWidth / 3, mouthVerticalOffset + smileHeight,
                    mouthWidth / 3, mouthVerticalOffset + smileHeight,
                    mouthWidth / 2, mouthVerticalOffset)
            canvas.drawPath(path, paint)
        }
    }

    internal class FaceState : BaseSavedState {

        internal var state = 0f

        constructor(superState: Parcelable) : super(superState)
        constructor(source: Parcel) : super(source) {
            state = source.readFloat()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeFloat(state)
        }

        companion object {
            val CREATOR = object : Parcelable.Creator<FaceState> {
                override fun createFromParcel(source: Parcel): FaceState {
                    return FaceState(source)
                }

                override fun newArray(size: Int): Array<FaceState> {
                    return newArray(size)
                }
            }
        }
    }
}
