package com.ssafy.jobis.presentation.login

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.ssafy.jobis.R
import com.ssafy.jobis.databinding.ActivityUserBinding
import com.ssafy.jobis.presentation.MainActivity
import com.ssafy.jobis.presentation.login.ui.login.LoginFragment
import com.ssafy.jobis.presentation.signup.ui.signup.SignupFragment

class UserActivity : AppCompatActivity() {

    val binding by lazy { ActivityUserBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.userFrame, LoginFragment())
            .commit()
    }

    fun goSignUp() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.userFrame, SignupFragment())
            .commit()
    }

    fun goLogin() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.userFrame, LoginFragment())
            .commit()
    }

    fun goMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun loadingOff() {
        binding.loading.visibility = View.GONE
    }
    fun loadingOn() {
        Log.d("test", "로딩 온!!")
        binding.loading.visibility = View.VISIBLE
    }
}

class CustomView(context: Context, attrs: AttributeSet): View(context, attrs) {
    private var mPath = Path()
    private var mPaint = Paint()
    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var firstCurveStartPoint: Point = Point()
    private var firstCurveEndPoint: Point = Point()
    private var secondCurveStartPoint: Point = Point()
    private var secondCurveEndPoint: Point = Point()
    private var thirdCurveStartPoint: Point = Point()
    private var thirdCurveEndPoint: Point = Point()
    private var firstCurveControlPoint1: Point = Point()
    private var firstCurveControlPoint2: Point = Point()
    private var secondCurveControlPoint1: Point = Point()
    private var secondCurveControlPoint2: Point = Point()
    private var thirdCurveControlPoint1: Point = Point()
    private var thirdCurveControlPoint2: Point = Point()
    private var circleSize: Int = 100
    private var circleColor: Int = -1
    private var bgColor: Int = -1


    init {
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.color = Color.WHITE
        setBackgroundColor(Color.TRANSPARENT)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mWidth = width
        mHeight = height

        firstCurveStartPoint.set(0, mHeight / 6)
        firstCurveEndPoint.set(mWidth / 4, mHeight / 3)
        secondCurveStartPoint = firstCurveEndPoint
        secondCurveEndPoint.set(mWidth / 2 + mWidth / 4, 0)
        thirdCurveStartPoint = secondCurveEndPoint
        thirdCurveEndPoint.set(mWidth, mHeight / 6)

        firstCurveControlPoint1.set(
            firstCurveStartPoint.x,
            firstCurveStartPoint.y
        )
        firstCurveControlPoint2.set(
            firstCurveEndPoint.x - (mWidth / 8),
            firstCurveEndPoint.y
        )
        secondCurveControlPoint1.set(
            secondCurveStartPoint.x + (mWidth / 4),
            secondCurveStartPoint.y
        )
        secondCurveControlPoint2.set(
            secondCurveEndPoint.x - (mWidth / 4),
            secondCurveEndPoint.y
        )
        thirdCurveControlPoint1.set(
            thirdCurveStartPoint.x + (mWidth / 8),
            thirdCurveStartPoint.y
        )
        thirdCurveControlPoint2.set(
            thirdCurveEndPoint.x,
            thirdCurveEndPoint.y
        )
        mPath.reset()
        mPath.moveTo(0f, (mHeight/3).toFloat())
        mPath.lineTo(firstCurveStartPoint.x.toFloat(), firstCurveStartPoint.y.toFloat())
        mPath.cubicTo(
            firstCurveControlPoint1.x.toFloat(), firstCurveControlPoint1.y.toFloat(),
            firstCurveControlPoint2.x.toFloat(), firstCurveControlPoint2.y.toFloat(),
            firstCurveEndPoint.x.toFloat(), firstCurveEndPoint.y.toFloat()
        )
        mPath.cubicTo(
            secondCurveControlPoint1.x.toFloat(), secondCurveControlPoint1.y.toFloat(),
            secondCurveControlPoint2.x.toFloat(), secondCurveControlPoint2.y.toFloat(),
            secondCurveEndPoint.x.toFloat(), secondCurveEndPoint.y.toFloat()
        )
        mPath.cubicTo(
            thirdCurveControlPoint1.x.toFloat(), thirdCurveControlPoint1.y.toFloat(),
            thirdCurveControlPoint2.x.toFloat(), thirdCurveControlPoint2.y.toFloat(),
            thirdCurveEndPoint.x.toFloat(), thirdCurveEndPoint.y.toFloat()
        )

        mPath.lineTo(mWidth.toFloat(), (mHeight / 6).toFloat())
        mPath.lineTo(mWidth.toFloat(), mHeight.toFloat())
        mPath.lineTo(0f, mHeight.toFloat())
        mPath.close()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawPath(mPath, mPaint)
    }
}