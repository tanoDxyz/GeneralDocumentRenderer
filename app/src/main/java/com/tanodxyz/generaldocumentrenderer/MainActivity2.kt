package com.tanodxyz.generaldocumentrenderer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller

class MainActivity2 : AppCompatActivity(), View.OnTouchListener {
    private lateinit var overScroller: OverScroller
    private lateinit var gestureDetector: GestureDetector
    private lateinit var simpleOnGestureListener: GestureDetector.SimpleOnGestureListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val view = findViewById<View>(R.id.view1)
        view.setOnTouchListener(this)
        overScroller = OverScroller(this)
        simpleOnGestureListener = object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                overScroller.startScroll(0,0,distanceX.toInt(),distanceY.toInt())
                return true
            }
        }
        gestureDetector = GestureDetector(this,simpleOnGestureListener)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }


}