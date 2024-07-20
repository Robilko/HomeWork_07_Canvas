package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Integer.min
import kotlin.math.atan2
import kotlin.math.max
import kotlin.random.Random

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var onCategoryClickListener: OnCategoryClickListener? = null

    fun setOnCategoryClickListener(listener: OnCategoryClickListener?) {
        onCategoryClickListener = listener
    }

    private val colors = arrayListOf(
        Color.RED,
        Color.GREEN,
        Color.GRAY,
        Color.CYAN,
        Color.MAGENTA,
        Color.YELLOW,
        Color.BLUE,
        Color.DKGRAY,
        Color.BLACK,
        Color.LTGRAY,
        Color.BLUE
    )

    private val defaultRadius = 50.asDp
    private var radius = defaultRadius
    private val strokeWidth = 50.asDp.toFloat()

    private var data: List<Category> = listOf()
    private var chartData: List<Category> = listOf()
    private var allDataSum = 0

    private var circleX = 0
    private var circleY = 0

    private val noDataPaint: Paint = Paint().apply {
        textSize = 72.asSp.toFloat()
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
    }
    private val defaultHeight = 100.asDp

    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = this@PieChartView.strokeWidth
    }

    private val chartRect = RectF()

    init {
        if (isInEditMode) setData(getMockList())
    }

    private fun getMockList(): List<Category> {
        val list = mutableListOf<Category>()
        repeat(SPECIFIC_CATEGORIES_MAX_COUNT) {
            list.add(Category("", Random.nextInt(100, 1000)))
        }
        return list
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        if (chartData.isEmpty()) {
            setMeasuredDimension(wSize, max(defaultHeight, hSize))
            return
        }

        val newW: Int = when (wMode) {
            MeasureSpec.EXACTLY -> wSize
            MeasureSpec.AT_MOST -> min(hSize, wSize)
            else -> {
                if (hMode == MeasureSpec.UNSPECIFIED) (defaultRadius + strokeWidth * 2).toInt()
                else hSize
            }
        }

        when (hMode) {
            MeasureSpec.EXACTLY -> setMeasuredDimension(newW, hSize)
            MeasureSpec.AT_MOST -> {
                if (newW < hSize) setMeasuredDimension(newW, newW)
                else setMeasuredDimension(newW, hSize)
            }

            else -> setMeasuredDimension(newW, newW)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (chartData.isEmpty()) {
            val textHeight = noDataPaint.fontMetrics.descent - noDataPaint.fontMetrics.ascent
            canvas.drawText(
                "No data!",
                (width / 2).toFloat(),
                height / 2 + textHeight / 3,
                noDataPaint
            )
            return
        }

        radius = max(min(width, height) - strokeWidth.toInt() * 2, defaultRadius) / 2

        circleX = width / 2
        circleY = height / 2

        chartRect.left = circleX - strokeWidth / 2 - radius
        chartRect.top = circleY - strokeWidth / 2 - radius
        chartRect.right = circleX + strokeWidth / 2 + radius
        chartRect.bottom = circleY + strokeWidth / 2 + radius

        var startAngle = 0f
        var endAngle: Float

        chartData.forEachIndexed { i, it ->
            endAngle =
                if (i == chartData.size) 360f else startAngle + (it.sum / allDataSum.toFloat() * 360)

            drawChartElement(canvas, startAngle, endAngle, colors[i])
            startAngle = endAngle
        }

    }

    private fun drawChartElement(canvas: Canvas, startAngle: Float, endAngle: Float, color: Int) {
        paint.color = color

        canvas.drawArc(chartRect, startAngle - 90, endAngle - startAngle, false, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val tX = event.x.toDouble()
            val tY = event.y.toDouble()

            val r1 = radius
            val r2 = radius + strokeWidth

            val l = (tX - circleX) * (tX - circleX) + (tY - circleY) * (tY - circleY)

            if (l >= r1 * r1 && l <= r2 * r2) {
                var angle = Math.toDegrees(atan2(tY - circleY, tX - circleX)) + 90
                if (angle < 0)
                    angle += 360

                var startAngle = 0.0

                chartData.forEachIndexed { i, it ->
                    val endAngle: Double = if (i == chartData.size) {
                        360.0
                    } else {
                        startAngle + (it.sum / allDataSum.toFloat() * 360)
                    }

                    if (angle > startAngle && angle < endAngle) {
                        onCategoryClickListener?.onClick(it)
                        performClick()
                        return true
                    }

                    startAngle = endAngle
                }
            }
        }

        return false
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putString(KEY_CATEGORIES, Gson().toJson(data))
        val superState = super.onSaveInstanceState()
        return SavedState(superState, bundle)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            val type = object : TypeToken<List<Category>>() {}.type
            data = Gson().fromJson(state.oneCategoryList, type)
            setData(data)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setData(categories: List<Category>) {
        data = categories
        val sorted = categories.sortedByDescending { it.sum }
        allDataSum = sorted.sumOf { it.sum }
        val list: MutableList<Category> = sorted.take(SPECIFIC_CATEGORIES_MAX_COUNT).toMutableList()
        sorted.drop(SPECIFIC_CATEGORIES_MAX_COUNT).let { otherCategories ->
            if (otherCategories.isNotEmpty())
                list.add(
                    Category(
                        name = resources.getString(
                            R.string.other_category,
                            otherCategories.map { it.name }),
                        sum = otherCategories.sumOf { it.sum },
                        isMultiple = true
                    )
                )
        }

        chartData = list
    }

    data class Category(
        val name: String,
        val sum: Int,
        val isMultiple: Boolean = false
    )

    interface OnCategoryClickListener {
        fun onClick(category: Category)
    }

    private class SavedState : BaseSavedState {

        var oneCategoryList: String = ""

        constructor(superState: Parcelable?, bundle: Bundle) : super(superState) {
            oneCategoryList = bundle.getString(KEY_CATEGORIES, "")
        }

        constructor(parcel: Parcel) : super(parcel) {
            oneCategoryList = parcel.readString() ?: ""
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeString(oneCategoryList)
        }

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    companion object {
        private const val SPECIFIC_CATEGORIES_MAX_COUNT = 9
        private const val KEY_CATEGORIES = "47b2de3d-467f-4512-8542-2df622c9baf8"
    }
}
