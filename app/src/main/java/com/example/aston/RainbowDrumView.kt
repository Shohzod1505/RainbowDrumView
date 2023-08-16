package com.example.aston

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
import kotlin.math.floor
import kotlin.random.Random

class RainbowDrumView(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int,
) : View(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : this(context, attributeSet, defStyleAttr, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context) : this(context, null)

    interface OnSetPhotoListener {
        fun setPhoto(currentColor: Int)
    }

    private val colorList: List<Int> = listOf(
        ContextCompat.getColor(context, R.color.red),
        ContextCompat.getColor(context, R.color.orange),
        ContextCompat.getColor(context, R.color.yellow),
        ContextCompat.getColor(context, R.color.green),
        ContextCompat.getColor(context, R.color.cyan),
        ContextCompat.getColor(context, R.color.blue),
        ContextCompat.getColor(context, R.color.purple),
    )
    private var rotatingDegree: Float = 270f
    private var centerX: Float = 0f
    private var centerY: Float = 0f
    private var radius: Float = 0f
    private val rectF = RectF()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintText = Paint().apply {
        textAlign = Paint.Align.CENTER
    }
    private var rotationAnimator: ValueAnimator? = null

    private val arrowDrawable = ContextCompat.getDrawable(context, R.drawable.arrow)
    private val arrowBitmap = drawableToBitmap(arrowDrawable)
    private var rotationAngleNew = 0f
    private var currentColor = 0
    private var currentText = ""
    var updatePhotoListener: OnSetPhotoListener? = null

    init {
        if (attributeSet != null) {
            initAttributes(attributeSet, defStyleAttr, defStyleRes)
        }
    }

    private fun initAttributes(attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {

        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RainbowDrumView, defStyleAttr, defStyleRes)

        centerX = typedArray.getDimension(R.styleable.RainbowDrumView_centerX, 0f)
        centerY = typedArray.getDimension(R.styleable.RainbowDrumView_centerY, 0f)
        radius = typedArray.getDimension(R.styleable.RainbowDrumView_radius, 100f)

        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.rotate(rotatingDegree, centerX, centerY)
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        for (colorIndex in colorList.indices) {
            paint.color = colorList[colorIndex]
            val startAngle = colorIndex * 360f / colorList.size
            canvas.drawArc(rectF, startAngle, 360f / colorList.size, true, paint)
        }

        canvas.restore()

        canvas.save()
        val arrowWidth = arrowBitmap.width
        val arrowHeight = arrowBitmap.height
        val arrowSize = radius * 0.01f
        val scaledArrowBitmap = createBitmap(arrowWidth, arrowHeight, arrowSize)
        val arrowX = centerX - scaledArrowBitmap.width / 2
        val arrowY = centerY - radius / 3 - scaledArrowBitmap.height / 2

        canvas.drawBitmap(scaledArrowBitmap, arrowX, arrowY, paint)
        canvas.restore()

        canvas.save()

        val backgroundPaint = paint.apply {
            color = Color.BLACK
        }

        val textX = centerX
        val textY = centerY / 4
        val textSizeSP = 36f
        val scale = resources.displayMetrics.scaledDensity
        val textSizePx = textSizeSP * scale
        val textPaint = paintText.apply {
            textSize = textSizePx
            color = colorList[currentColor]
        }

        canvas.drawRect(textX - 250, textY - 100, textX + 250, textY + 25, backgroundPaint)

        if (currentColor in listOf(0, 2, 4, 6)) {
            canvas.drawText(currentText, textX, textY, textPaint)
        }

        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredRadius = radius.toInt()
        val desiredDiameter = desiredRadius * 2

        val desiredWidth = desiredDiameter + paddingStart + paddingEnd
        val desiredHeight = desiredDiameter + paddingTop + paddingBottom

        val measuredWidth = resolveSizeAndState(desiredWidth, widthMeasureSpec, 0)
        val measuredHeight = resolveSizeAndState(desiredHeight, heightMeasureSpec, 0)

        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    private fun determineArrowColor(rotationAngle: Float) {

        rotationAngleNew += rotationAngle

        if (rotationAngleNew >= 360) {
            rotationAngleNew %= 360
        }

        val sectorSize = 360f / colorList.size
        val arrowSectorIndex = rotationAngleNew / sectorSize
        val arrowColorIndex = floor((colorList.size - arrowSectorIndex) % colorList.size)

        val colorName = when (arrowColorIndex.toInt()) {
            0 -> "RED"
            2 -> "YELLOW"
            4 -> "CYAN"
            6 -> "PURPLE"
            else -> {
                ""
            }
        }
        currentText = colorName
        currentColor = arrowColorIndex.toInt()
    }

    fun spin(): Int {
        if (rotationAnimator == null || !rotationAnimator!!.isRunning) {

            val targetDegree = rotatingDegree + 360f * 3 + Random.nextInt(360)
            val colorDegree = rotatingDegree

            rotationAnimator = ValueAnimator.ofFloat(rotatingDegree, targetDegree)
            rotationAnimator?.apply {
                duration = 2000
                interpolator = LinearInterpolator()
                addUpdateListener { animator ->
                    rotatingDegree = animator.animatedValue as Float
                    invalidate()
                }
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        determineArrowColor((targetDegree - colorDegree) - 360f * 3)
                        updatePhotoListener?.setPhoto(currentColor)
                        invalidate()
                    }
                })
                start()
            }
        }
        return currentColor
    }

    fun updateRadius(newRadius: Float) {
        radius = newRadius
        requestLayout()
    }

    fun clearText() {
        currentText = ""
        invalidate()
    }

    private fun createBitmap(arrowWidth: Int, arrowHeight: Int, arrowSize: Float): Bitmap =
        Bitmap.createScaledBitmap(
            arrowBitmap,
            (arrowWidth * arrowSize).toInt(),
            (arrowHeight * arrowSize).toInt(),
            true
        )

    private fun drawableToBitmap(drawable: Drawable?): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap = Bitmap.createBitmap(
            drawable?.intrinsicWidth ?: 1,
            drawable?.intrinsicHeight ?: 1,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable?.setBounds(0, 0, canvas.width, canvas.height)
        drawable?.draw(canvas)
        return bitmap
    }

}
