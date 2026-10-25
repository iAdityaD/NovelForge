package com.novelforge.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/** Copies a bounded, normalized image locally instead of retaining a temporary picker URI. */
@Singleton
class CoverImages @Inject constructor(@ApplicationContext private val context: Context) {
    suspend fun import(source: String): String = withContext(Dispatchers.IO) {
        val bytes = requireNotNull(context.contentResolver.openInputStream(Uri.parse(source))).use { input ->
            val out = ByteArrayOutputStream(); val buffer = ByteArray(8192)
            while (true) { val count = input.read(buffer); if (count < 0) break
                require(out.size() + count <= 20 * 1024 * 1024) { "Choose an image smaller than 20 MB." }
                out.write(buffer, 0, count)
            }; out.toByteArray()
        }
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        require(options.outWidth > 0 && options.outHeight > 0) { "Choose a supported image." }
        options.inSampleSize = 1
        while (maxOf(options.outWidth, options.outHeight) / options.inSampleSize > 1024) options.inSampleSize *= 2
        options.inJustDecodeBounds = false
        val original = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options))
        val orientation = runCatching { ExifInterface(bytes.inputStream()).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1) }.getOrDefault(1)
        val matrix = Matrix().apply {
            when (orientation) {
                2 -> setScale(-1f, 1f)
                3 -> setRotate(180f)
                4 -> setScale(1f, -1f)
                5 -> { setRotate(90f); postScale(-1f, 1f) }
                6 -> setRotate(90f)
                7 -> { setRotate(-90f); postScale(-1f, 1f) }
                8 -> setRotate(-90f)
            }
        }
        val bitmap = if (orientation in 2..8) Bitmap.createBitmap(original, 0, 0, original.width, original.height, matrix, true) else original
        try {
            val output = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP).also(::validate)
        } finally { if (bitmap !== original) bitmap.recycle(); original.recycle() }
    }
    companion object {
        fun validate(value: String) {
            if (value.isEmpty()) return
            require(value.length <= 750000) { "Cover image is too large." }
            val bytes = Base64.decode(value, Base64.NO_WRAP)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            require(options.outWidth in 1..2048 && options.outHeight in 1..2048) { "Invalid cover image." }
        }
    }
}
