package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import java.util.*
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var sizeWidth = 150
    private var sizeHeight = 100
    private var animatorValue = ValueAnimator()
    private var txtColor: Int = Color.WHITE
    private var bgColorLoading: Int = 0
    private var backGroundColor: Int = 0

    private val rectDraw = RectF(
        750f,
        40f,
        820f,
        110f
    )


     private val myPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { //
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        typeface = Typeface.create("", Typeface.BOLD)
    }

    @Volatile
    private var prog: Int = 0

    init {

        isClickable = true
        animatorValue = ValueAnimator.ofInt(0, 100).apply {
            duration = 300
            interpolator = LinearInterpolator()
            addUpdateListener {
                prog = this.animatedValue as Int
                invalidate()
                requestLayout()
            }
        }

        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            backGroundColor = getColor(R.styleable.LoadingButton_defaultButton_color, 0)
            bgColorLoading = getColor(R.styleable.LoadingButton_loadingButton_color, 0)
        }
    }

    var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, s ->
        when (s) {
            ButtonState.Completed -> {
                animatorValue.cancel()
                invalidate()
                requestLayout()
            }

            ButtonState.Loading -> {
                animatorValue.start()
                invalidate()
                requestLayout()
            }

        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        myPaint.color = backGroundColor

        if (buttonState === ButtonState.Loading) {
            //Drawing Loading Button
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), myPaint)
            myPaint.color = bgColorLoading
            canvas.drawRect(
                0f, 0f,
                (width * (prog.toDouble() / 100)).toFloat(), height.toFloat(), myPaint
            )
            //Drawing arc Loading Button
            myPaint.color =  ContextCompat.getColor(context,R.color.colorAccent)
            canvas.drawArc(rectDraw, 0f, (360 * (prog.toDouble() / 100)).toFloat(), true, myPaint)
        } else {

            //drawing Normal Default Button
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), myPaint)
        }

        myPaint.color = txtColor
        val textHeight = (height / 2 - (myPaint.descent() + myPaint.ascent()) / 2)
        val buttonLabel =
            if (buttonState === ButtonState.Loading)
                resources.getString(R.string.button_loading)
            else
                resources.getString(R.string.button_name)
        canvas.drawText(buttonLabel.toUpperCase(Locale.ROOT), (width / 2).toFloat(), textHeight, myPaint
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
        sizeWidth = w
        sizeHeight = h
        setMeasuredDimension(w, h)

    }
    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

}