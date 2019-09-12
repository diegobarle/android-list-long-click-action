package uk.co.diegobarle.longclicklistanimation

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View

fun View.takeScreenshot(): Bitmap? {
    if(width <= 0 || height <= 0){
        return null
    }
    return try {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        draw(c)
        b
    }catch (error: OutOfMemoryError){
        //If we ran out of memory, at least return no image
        null
    }
}