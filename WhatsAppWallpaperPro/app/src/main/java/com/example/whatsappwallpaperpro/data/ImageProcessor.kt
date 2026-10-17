package com.example.whatsappwallpaperpro.data

import android.content.ContentResolver
import android.graphics.*
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.example.whatsappwallpaperpro.domain.BackgroundMode
import com.example.whatsappwallpaperpro.domain.OutputSize
import kotlin.math.max

object ImageProcessor {
    fun decode(resolver: ContentResolver, uri: Uri, maxSide: Int = 4096): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "الصورة غير صالحة أو تالفة" }
        var sample = 1
        while (max(bounds.outWidth / sample, bounds.outHeight / sample) > maxSide) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample; inPreferredConfig = Bitmap.Config.ARGB_8888 }
        val decoded = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, opts) }
            ?: error("تعذر قراءة الصورة")
        return applyExif(resolver, uri, decoded)
    }

    private fun applyExif(resolver: ContentResolver, uri: Uri, source: Bitmap): Bitmap {
        val orientation = runCatching { resolver.openInputStream(uri).use { ExifInterface(it!!).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) } }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
        val matrix = Matrix()
        when (orientation) { ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f); ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f); ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f); ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f,1f); ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f,-1f) }
        return if (!matrix.isIdentity) Bitmap.createBitmap(source,0,0,source.width,source.height,matrix,true) else source
    }

    fun render(source: Bitmap, size: OutputSize, mode: BackgroundMode, customColor: Int, zoom: Float = 1f, offsetX: Float = 0f, offsetY: Float = 0f): Bitmap {
        val output = Bitmap.createBitmap(size.width, size.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        when (mode) {
            BackgroundMode.BLUR -> { canvas.drawColor(smartColor(source)); val small = Bitmap.createScaledBitmap(source, max(1,size.width/24), max(1,size.height/24), true); val bg = Bitmap.createScaledBitmap(small,size.width,size.height,true); canvas.drawBitmap(bg,0f,0f,paint); small.recycle() }
            BackgroundMode.SMART_COLOR -> canvas.drawColor(smartColor(source))
            BackgroundMode.CUSTOM -> canvas.drawColor(customColor)
        }
        val fit = minOf(size.width.toFloat()/source.width, size.height.toFloat()/source.height) * zoom
        val left = (size.width - source.width*fit)/2f + offsetX
        val top = (size.height - source.height*fit)/2f + offsetY
        canvas.drawBitmap(source, null, RectF(left,top,left+source.width*fit,top+source.height*fit), paint)
        return output
    }

    fun smartColor(bitmap: Bitmap): Int {
        val sample = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
        val color = sample.getPixel(0,0); sample.recycle(); return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
    }
}
