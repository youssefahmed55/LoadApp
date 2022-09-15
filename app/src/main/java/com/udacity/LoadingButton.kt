package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.withStyledAttributes
import java.util.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 100
    private var heightSize = 50
    private var bgColor: Int = 0
    private var bgColorLoading: Int = 0
    private var bgColorDisabled: Int = 0
    private var txtColor: Int = Color.WHITE
    private var valueAnimator = ValueAnimator()

    @Volatile
    private var progress: Int = 0 //

    private val rect = RectF(
        740f,
        40f,
        810f,
        110f
    )


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { //
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    init {
        isClickable = true

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            bgColor = getColor(R.styleable.LoadingButton_button_color, 0)
            bgColorDisabled = getColor(R.styleable.LoadingButton_button_disable_color, 0)
            bgColorLoading = getColor(R.styleable.LoadingButton_button_loading_color, 0)
        }
        valueAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            addUpdateListener { valueAnimator ->
                progress = this.animatedValue as Int
                invalidate()
                requestLayout()
            }
        }
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
        when (new) {
            ButtonState.Loading -> {
                valueAnimator.start()
                invalidate()
                requestLayout()
            }
            ButtonState.Completed -> {
                valueAnimator.cancel()
                invalidate()
                requestLayout()
            }
            ButtonState.Disabled -> {
                valueAnimator.cancel()
                invalidate()
                requestLayout()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //Drawing Button
        val width = width
        val height = height
        paint.color =
            if (buttonState === ButtonState.Disabled)
                bgColorDisabled
            else
                bgColor
        if (buttonState === ButtonState.Loading) {

            //Drawing button loading
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.color = bgColorLoading
            canvas.drawRect(
                0f, 0f,
                (width * (progress.toDouble() / 100)).toFloat(), height.toFloat(), paint
            )
            //Drawing loading arc
            paint.color = resources.getColor(R.color.colorAccent)
            canvas.drawArc(rect, 0f, (360 * (progress.toDouble() / 100)).toFloat(), true, paint)
        } else {

            //drawing normal button
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        }

        paint.color = txtColor
        val textHeight = (height / 2 - (paint.descent() + paint.ascent()) / 2)
        val buttonLabel =
            if (buttonState === ButtonState.Loading)
                resources.getString(R.string.button_loading)
            else
                resources.getString(R.string.button_name)
        canvas.drawText(
            buttonLabel.toUpperCase(Locale.ROOT),
            (width / 2).toFloat(),
            textHeight,
            paint
        )


    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)

    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}