package com.tanodxyz.documentrenderer.page

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.widget.Toast
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.elements.IElement
import com.tanodxyz.documentrenderer.events.FlingEndEvent
import com.tanodxyz.documentrenderer.events.IEvent
import com.tanodxyz.documentrenderer.events.IEventHandler
import com.tanodxyz.documentrenderer.getHeight
import com.tanodxyz.documentrenderer.getWidth
import java.io.Serializable
import java.security.SecureRandom
import java.util.*
import kotlin.math.abs

data class DocumentPage(
    val uniquieID: Int = -1,
    val elements: MutableList<IElement> = mutableListOf(),
    val originalSize: Size = Size(
        3555,
        2666
    ),
    val pageBounds: RectF = RectF(0F, 0F, 0F, 0F),
    val documentPageRequestHandler: DocumentRenderView.DocumentPageRequestHandler
) : Serializable,IEventHandler {

    val pagePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    var size: Size = originalSize

    fun getWidth(): Float {
        return pageBounds.getWidth()
    }

    fun getHeight(): Float {
        return pageBounds.getHeight()
    }

    fun draw(canvas: Canvas, draw: Boolean) {
        if(draw) {
            val color = if(draw) Color.GREEN else Color.RED // green indicates drawing // red is against green
            val rectF = RectF(pageBounds.left + 100,pageBounds.top + 100,pageBounds.right - 100,pageBounds.bottom - 100)
            pagePaint.color = color
            canvas.drawRect(rectF, pagePaint)
            pagePaint.color = Color.RED
            pagePaint.textSize = 23F
            canvas.drawText("page No ${uniquieID +1}",pageBounds.left + 30,pageBounds.top + 30,pagePaint)
        } else {
            // .TODO RECYCLE PAGE.
        }
    }

    override fun onEvent(event: IEvent?) {
        println("UYT: event recieved for ${uniquieID} | is $event")
    }

    fun resetPageBounds() {
        pageBounds.top = 0F
        pageBounds.left = 0F
        pageBounds.right = 0F
        pageBounds.bottom = 0F
    }


//    companion object {
//        fun newPageWithWidthEqualsScreenWidth(context: Context, height: Int): DocumentPage {
//            val widthPixels = context.resources.displayMetrics.widthPixels
//            return DocumentPage(originalSize = Size(widthPixels, height))
//        }
//
//        fun newPageWithHeightEqualsScreenHeight(context: Context, width: Int): DocumentPage {
//            val heightPixels = context.resources.displayMetrics.heightPixels
//            return DocumentPage(originalSize = Size(width, heightPixels))
//        }
//    }

}