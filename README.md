Introduction
--------

General Document Renderer (GDR) is a tiny library that allows general content to be rendered to the canvas in the form of pages.  
e.g.  
*  you want to render pdf file and you have pdf pages in the form of bitmaps or pngs.  
*  you want a paint app.  
*  you want to load a content of a large file in the text format.  
*  you want to create a simple slide show.  
and many more ...  

#### Following Screenshots are taken from examples that are included in sample app.  
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/main_.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/pdf_reader.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/file_reader_1.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/file_reader_2.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/canvas.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/photo_1.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/photo_2.png)
![ScreenShot](https://github.com/tanoDxyz/GeneralDocumentRenderer/blob/main/custom_element.png)


#### Add to Project
Using GDR is easy! Just add the following to your **application's root build.gradle** file or **settings.gradle**
``` kotlin

 allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
 }
```
And then add the dependency to the module level build.gradle file.

``` kotlin
implementation 'com.github.tanoDxyz:GeneralDocumentRenderer:v0.1'
```

## Usage
First document is created and than pages are added to it.  
Each page can have many types of elements (pdfElement,textElement,ImageElement or you own elements.)  
after that document is loaded into documentrenderView and that's it.  

``` kotlin
	val documentView = findViewByiD(...)
	val document = Document(context)
	document.pageFitPolicy = BOTH
	val page = DocumentPage(uniqueId,Size(400,900))
	val imageElement = ImageElement()
	imageElement.loadBitmap(bitmap)
	page.elements.add(imageElement)
	document.addPage(page)
	documentView.loadDocument(document)

```


## Features

### Page
    can be scrolled verticall or horizontaly.
    can be scaled or zoomed via scale gesture or double tap.
    can have margins and round corners as well.
    background can have different colors.
    supports both scroll and fling.
    supports PageFling (one fling and swipe one Page.)  
 
 
* Support For view extensions (views can be attached to RenderView)  
* Total/Current Page display widget  
* ScrollBar widget  
* Each page can have snapshot. (save to disk , render onScale etc)  
* Easy to extend or change Individual behaviours.  
* Busy state Indicator.  
* Can easily write custom elements.  

  
  
  
  

### Important Definitions
#### DocumentPage
it is a single page in the document and have the following properties.  
Each DocumentPage has a unique id which starts from 0.  
Elements which are going to be rendered by DocumentRenderView  
Bounds on the screen.(left,top,right,bottom)  
Page size.(width,height)  
PageSnapShot which is going to be rendered when page is scaled using scale/zoom gesture  


#### InteractiveElement
Each page can have multiple graphic elements that can be drawn to canvas relative to page bounds and of course recieve different Events.  
check out documentation for more details.



#### PageElement
Implementation of InteractiveElement that provides some basic utitilities i.e. handling of events and calculating content bounds relative to page.



#### Creating Custom Elements Using PageElement Class

##### Example 1
``` kotlin 
// simiple element that renders a rectangle on screen
// properly scaled when user zoom in / zoom out
// properly shift it's coordinates when multiple pages are present in the document
open class CustomElement(page: DocumentPage) : PageElement(page) {
        val width = 128 //px
        val height = 128 // px

        init {
            debugPaint.apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            }
        }

        override fun getContentHeight(args: SparseArray<Any>?): Float {
            return page.documentRenderView.toCurrentScale(width)
        }

        override fun getContentWidth(args: SparseArray<Any>?): Float {
            return page.documentRenderView.toCurrentScale(height)
        }

        override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
            super.draw(canvas, args)
            // args.shouldDrawSnapShot indicates whether page is currently scaled and page is drawing snapshot instead of original content.
            val contentBounds = getContentBounds(args.shouldDrawSnapShot())

            canvas.drawRect(contentBounds, debugPaint)
        }
    }


```

##### Example 2
``` kotlin

// simple rectangle as previous example but when clicked  
// textElement display event related data.
class CustomInteractiveElement(page:DocumentPage):CustomElementActivity.CustomElement(page),
PageElement.OnClickListener {

        private var textElement = SimpleTextElement(page).apply {
            textColor = Color.WHITE
            setText("Tap Green Rectangle".toSpannable())
            margins.apply {
                top = 10F
            }
        }
        
        init {
            // on Long press element can be moved across page.
            movable = true
            clickListener = this
        }

        override fun draw(canvas: Canvas, args: SparseArray<Any>?) {
            super.draw(canvas, args)
            textElement.draw(canvas,args)
        }

        override fun onClick(eventMarker: SingleTapConfirmedEvent?, pageElement: PageElement) {
            textElement.setText("${pageElement.type} clicked $eventMarker".toSpannable())
        }
    }

```

