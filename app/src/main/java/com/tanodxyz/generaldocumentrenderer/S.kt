package com.tanodxyz.generaldocumentrenderer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class S @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), View.OnTouchListener {
    init {
        setOnTouchListener(this)
    }
    var offY = 10f
    var offX = 10f
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setColor(Color.GREEN)
        paint.style = Paint.Style.FILL_AND_STROKE

        if(offY > 0) {
            canvas?.translate(10f,offY)
            canvas?.drawCircle(50f,0f,80f,paint)
            canvas?.scale(offX,offY)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event?.actionMasked) {
            MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE -> {
                offY = event.y
                offX = event.x
                invalidate()
                println("Bako: moving to $offY")
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL ->{
                offY = -1f
                invalidate()
            }
        }
        return true
    }


}