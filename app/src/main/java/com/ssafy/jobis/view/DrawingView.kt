package com.ssafy.jobis.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {
    companion object {
        var color = Color.BLACK
        var size = 5 // 선 굵기 기본값
    }

    var sX = -1
    var sY = -1
    var eX = -1
    var eY = -1
    private var isFinished = false
    private var path: Path? = null
    private var paint: Paint? = null
    private val lineList = ArrayList<Line>()
    private val stack = Stack<Line>()

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {

                stack.clear()

                path = Path()
                sX = event.x.toInt()
                sY = event.y.toInt()
                path?.moveTo(sX.toFloat(), sY.toFloat())

                paint = Paint() // paint라는 객체를 생성하고
                paint?.also {
                    it.style = Paint.Style.STROKE // 채워지지 않는 도형 형성
                    it.strokeWidth = size.toFloat()
                    it.color = color
                    lineList.add(Line(path!!, it))
                }

                isFinished = false
            }
            MotionEvent.ACTION_MOVE -> {
                eX = event.x.toInt()
                eY = event.y.toInt()
                path?.lineTo(eX.toFloat(), eY.toFloat())

                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                isFinished = true
                paint = null
                path = null
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        for (line in lineList) {
            canvas?.drawPath(line.path, line.paint)
        }
        if (paint != null && path != null) {
            canvas?.drawPath(path!!, paint!!)
        }
    }

    fun unDo() {
        if (lineList.isNotEmpty()) {
            val line = lineList.last()
            lineList.remove(line)
            stack.push(line)
            invalidate()
        }
    }

    fun reDo() {
        if (stack.isNotEmpty()) {
            val line = stack.pop()
            lineList.add(line)
            invalidate()
        }
    }

    data class Line(var path: Path, var paint: Paint)
}