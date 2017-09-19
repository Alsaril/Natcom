package com.natcom.fragment

import android.content.Context
import android.graphics.Matrix
import android.graphics.PointF
import android.os.Bundle
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.natcom.POSITION_KEY
import com.natcom.R
import com.natcom.activity.LeadController
import com.natcom.model.Picture
import kotterknife.bindView
import java.util.*


class FullscreenFragment : CustomFragment() {
    private val pager by bindView<ViewPager>(R.id.pager)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        initFragment(inflater.inflate(R.layout.fullscreen_fragment, container, false), "")

        pager.adapter = FullscreenPagerAdapter((activity as LeadController).lead().images, this)

        arguments?.getInt(POSITION_KEY)?.let {
            pager.currentItem = it
        }

        (activity as AppCompatActivity).supportActionBar?.hide()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        (activity as AppCompatActivity).supportActionBar?.show()
    }
}

class FullscreenPagerAdapter(urlList: ArrayList<Picture>, private val fullscreenFragment: FullscreenFragment) : PagerAdapter() {

    val list: List<Picture> = Collections.unmodifiableList(urlList)

    override fun isViewFromObject(view: View, `object`: Any): Boolean =
            view === `object` as TouchImageView

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val image = TouchImageView(fullscreenFragment.context)
        Glide.with(fullscreenFragment).load(list[position].url).into(image)
        (container as ViewPager).addView(image)
        return image
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) = (container as ViewPager).removeView(`object` as TouchImageView)

    override fun getCount() = list.size
}

open class TouchImageView : ImageView {

    lateinit internal var matrix: Matrix
    internal var mode = NONE

    // Remember some things for zooming
    private var last = PointF()
    private var start = PointF()
    private var minScale = 1f
    internal var maxScale = 3f
    private lateinit var m: FloatArray

    internal var viewWidth: Int = 0
    internal var viewHeight: Int = 0
    internal var saveScale = 1f
    private var origWidth: Float = 0.toFloat()
    private var origHeight: Float = 0.toFloat()
    private var oldMeasuredWidth: Int = 0
    private var oldMeasuredHeight: Int = 0

    lateinit internal var mScaleDetector: ScaleGestureDetector

    lateinit internal var context: Context

    constructor(context: Context) : super(context) {
        sharedConstructing(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        sharedConstructing(context)
    }

    private fun sharedConstructing(context: Context) {
        super.setClickable(true)
        this.context = context
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())
        matrix = Matrix()
        m = FloatArray(9)
        imageMatrix = matrix
        scaleType = ImageView.ScaleType.MATRIX

        setOnTouchListener { v, event ->
            mScaleDetector.onTouchEvent(event)
            val curr = PointF(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    last.set(curr)
                    start.set(last)
                    mode = DRAG
                }

                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    val deltaX = curr.x - last.x
                    val deltaY = curr.y - last.y
                    val fixTransX = getFixDragTrans(deltaX, viewWidth.toFloat(),
                            origWidth * saveScale)
                    val fixTransY = getFixDragTrans(deltaY, viewHeight.toFloat(),
                            origHeight * saveScale)
                    matrix.postTranslate(fixTransX, fixTransY)
                    fixTrans()
                    last.set(curr.x, curr.y)
                }

                MotionEvent.ACTION_UP -> {
                    mode = NONE
                    val xDiff = Math.abs(curr.x - start.x).toInt()
                    val yDiff = Math.abs(curr.y - start.y).toInt()
                    if (xDiff < CLICK && yDiff < CLICK)
                        performClick()
                }

                MotionEvent.ACTION_POINTER_UP -> mode = NONE
            }

            imageMatrix = matrix
            invalidate()
            true // indicate event was handled
        }
    }

    fun setMaxZoom(x: Float) {
        maxScale = x
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            mode = ZOOM
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            var mScaleFactor = detector.scaleFactor
            val origScale = saveScale
            saveScale *= mScaleFactor
            if (saveScale > maxScale) {
                saveScale = maxScale
                mScaleFactor = maxScale / origScale
            } else if (saveScale < minScale) {
                saveScale = minScale
                mScaleFactor = minScale / origScale
            }

            if (origWidth * saveScale <= viewWidth || origHeight * saveScale <= viewHeight)
                matrix.postScale(mScaleFactor, mScaleFactor, viewWidth.toFloat() / 2,
                        viewHeight.toFloat() / 2)
            else
                matrix.postScale(mScaleFactor, mScaleFactor,
                        detector.focusX, detector.focusY)

            fixTrans()
            return true
        }
    }

    internal fun fixTrans() {
        matrix.getValues(m)
        val transX = m[Matrix.MTRANS_X]
        val transY = m[Matrix.MTRANS_Y]

        val fixTransX = getFixTrans(transX, viewWidth.toFloat(), origWidth * saveScale)
        val fixTransY = getFixTrans(transY, viewHeight.toFloat(), origHeight * saveScale)

        if (fixTransX != 0f || fixTransY != 0f)
            matrix.postTranslate(fixTransX, fixTransY)
    }

    private fun getFixTrans(trans: Float, viewSize: Float, contentSize: Float): Float {
        val minTrans: Float
        val maxTrans: Float

        if (contentSize <= viewSize) {
            minTrans = 0f
            maxTrans = viewSize - contentSize
        } else {
            minTrans = viewSize - contentSize
            maxTrans = 0f
        }

        if (trans < minTrans)
            return -trans + minTrans
        if (trans > maxTrans)
            return -trans + maxTrans
        return 0f
    }

    private fun getFixDragTrans(delta: Float, viewSize: Float, contentSize: Float): Float {
        if (contentSize <= viewSize) {
            return 0f
        }
        return delta
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        viewWidth = View.MeasureSpec.getSize(widthMeasureSpec)
        viewHeight = View.MeasureSpec.getSize(heightMeasureSpec)

        if (oldMeasuredHeight == viewWidth && oldMeasuredHeight == viewHeight
                || viewWidth == 0 || viewHeight == 0)
            return
        oldMeasuredHeight = viewHeight
        oldMeasuredWidth = viewWidth

        if (saveScale == 1f) {
            val scale: Float

            val drawable = drawable
            if (drawable == null || drawable.intrinsicWidth == 0
                    || drawable.intrinsicHeight == 0)
                return
            val bmWidth = drawable.intrinsicWidth
            val bmHeight = drawable.intrinsicHeight

            val scaleX = viewWidth.toFloat() / bmWidth.toFloat()
            val scaleY = viewHeight.toFloat() / bmHeight.toFloat()
            scale = Math.min(scaleX, scaleY)
            matrix.setScale(scale, scale)

            // Center the image
            var redundantYSpace = viewHeight.toFloat() - scale * bmHeight.toFloat()
            var redundantXSpace = viewWidth.toFloat() - scale * bmWidth.toFloat()
            redundantYSpace /= 2.toFloat()
            redundantXSpace /= 2.toFloat()

            matrix.postTranslate(redundantXSpace, redundantYSpace)

            origWidth = viewWidth - 2 * redundantXSpace
            origHeight = viewHeight - 2 * redundantYSpace
            imageMatrix = matrix
        }
        fixTrans()
    }

    companion object {
        internal val NONE = 0
        internal val DRAG = 1
        internal val ZOOM = 2
        internal val CLICK = 3
    }
}