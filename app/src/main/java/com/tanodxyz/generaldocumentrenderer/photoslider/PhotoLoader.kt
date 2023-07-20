package com.tanodxyz.generaldocumentrenderer.photoslider

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Looper
import com.tanodxyz.documentrenderer.DocumentRenderView
import com.tanodxyz.documentrenderer.Size
import com.tanodxyz.documentrenderer.document.Document
import com.tanodxyz.documentrenderer.elements.InteractiveElement
import com.tanodxyz.documentrenderer.page.DocumentPage
import com.tanodxyz.generaldocumentrenderer.R
import kotlin.concurrent.thread


class PhotoLoader(val renderView: DocumentRenderView) {
    private var document: Document? = null

    fun prepareDocument(loadInRenderView:Boolean,onFinish:()->Unit) {

        val handler = android.os.Handler(Looper.getMainLooper())
        renderView.setBackgroundColor(Color.BLACK)

        thread {
            val resources = renderView.resources
            val displayMetrics = resources.displayMetrics
            val options: BitmapFactory.Options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.a, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _a: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.a, options)


            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.b, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _b: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.b, options)


            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.c, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _c: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.c, options)


            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.d, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _d: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.d, options)


            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.e, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _e: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.e, options)



            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.f, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _f: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.f, options)



            options.inJustDecodeBounds = true
            BitmapFactory.decodeResource(resources, R.drawable.g, options)
            options.inSampleSize = calculateInSampleSize(options,displayMetrics.widthPixels,displayMetrics.heightPixels)
            options.inJustDecodeBounds = false

            val _g: Bitmap =
                BitmapFactory.decodeResource(resources, R.drawable.g, options)

            document = Document(renderView.context)
            document?.documentFitPagePolicy = Document.PageFitPolicy.BOTH

            document!!.swipeVertical = false

            document!!.fitEachPage = true

            document!!.pageFling = true

            val elements1 = mutableListOf<InteractiveElement>()
            val page1 = DocumentPage(0,elements1, Size(_a.width,_a.height))
            val imageElement1 = PhotoElement(page1)
            imageElement1.load(_a,false)
            elements1.add(imageElement1)
            page1.setUseScalingFactorForSnapshot(false)
            document?.addPage(page1)


            val elements2 = mutableListOf<InteractiveElement>()
            val page2 = DocumentPage(1,elements2, Size(_b.width,_b.height))
            val imageElement2 = PhotoElement(page2)
            imageElement2.load(_b,false)
            elements2.add(imageElement2)
            page2.setUseScalingFactorForSnapshot(false)
            document?.addPage(page2)

            val elements3 = mutableListOf<InteractiveElement>()
            val page3 = DocumentPage(2,elements3, Size(_c.width,_c.height))
            val imageElement3 = PhotoElement(page3)
            imageElement3.load(_c,false)
            elements3.add(imageElement3)
            page3.setUseScalingFactorForSnapshot(false)
            document?.addPage(page3)


            val elements4 = mutableListOf<InteractiveElement>()
            val page4 = DocumentPage(3,elements4, Size(_d.width,_d.height))
            val imageElement4 = PhotoElement(page4)
            imageElement4.load(_d,false)
            elements4.add(imageElement4)
            page4.setUseScalingFactorForSnapshot(false)

            document?.addPage(page4)

            val elements5 = mutableListOf<InteractiveElement>()
            val page5 = DocumentPage(4,elements5, Size(_e.width,_e.height))
            val imageElement5 = PhotoElement(page5)
            imageElement5.load(_e,false)
            elements5.add(imageElement5)
            page5.setUseScalingFactorForSnapshot(false)

            document?.addPage(page5)

            val elements6 = mutableListOf<InteractiveElement>()
            val page6 = DocumentPage(5,elements6, Size(_f.width,_f.height))
            val imageElement6 = PhotoElement(page6)
            imageElement6.load(_f,false)
            elements6.add(imageElement6)
            page6.setUseScalingFactorForSnapshot(false)

            document?.addPage(page6)

            val elements7 = mutableListOf<InteractiveElement>()
            val page7 = DocumentPage(6,elements7, Size(_g.width,_g.height))
            val imageElement7 = PhotoElement(page7)
            imageElement7.load(_g,false)
            elements7.add(imageElement7)
            page7.setUseScalingFactorForSnapshot(false)

            document?.addPage(page7)

            if(loadInRenderView) {
                renderView.loadDocument(document!!,onFinish)
            } else {
                handler.post(onFinish)
            }
        }
    }


    // code from chat gpt
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
    fun close() {
        document?.close()
    }
}

