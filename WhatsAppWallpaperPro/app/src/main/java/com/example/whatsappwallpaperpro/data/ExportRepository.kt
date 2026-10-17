package com.example.whatsappwallpaperpro.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import com.example.whatsappwallpaperpro.domain.OutputFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportRepository(private val context: Context) {
    fun save(bitmap: Bitmap, format: OutputFormat, quality: Int): Result<String> = runCatching {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val name = "WhatsApp_Wallpaper_$stamp.${format.extension}"
        val values = ContentValues().apply { put(MediaStore.Images.Media.DISPLAY_NAME,name); put(MediaStore.Images.Media.MIME_TYPE, if(format == OutputFormat.PNG) "image/png" else if(format == OutputFormat.WEBP) "image/webp" else "image/jpeg"); put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES+"/WhatsApp Wallpaper Pro"); put(MediaStore.Images.Media.IS_PENDING,1) }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: error("تعذر إنشاء ملف في المعرض")
        try { context.contentResolver.openOutputStream(uri).use { out -> requireNotNull(out); val compress = when(format){OutputFormat.PNG->Bitmap.CompressFormat.PNG;OutputFormat.JPG->Bitmap.CompressFormat.JPEG;OutputFormat.WEBP->Bitmap.CompressFormat.WEBP}; check(bitmap.compress(compress, quality, out)) { "تعذر ضغط الصورة" } }; values.clear(); values.put(MediaStore.Images.Media.IS_PENDING,0); context.contentResolver.update(uri,values,null,null); uri.toString() } catch (e: Exception) { context.contentResolver.delete(uri,null,null); throw e }
    }.map { it }
}
