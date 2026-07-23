package org.nimio.app.core.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun importCroppedAvatar(
    context: Context,
    sourceUri: Uri
): String? = withContext(Dispatchers.IO) {
    runCatching {
        val resolver = context.contentResolver
        val bitmap = resolver.openInputStream(sourceUri)?.use { BitmapFactory.decodeStream(it) }
            ?: return@withContext null

        val size = minOf(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        val square = Bitmap.createBitmap(bitmap, x, y, size, size)

        val output = if (square.width > 1024) {
            Bitmap.createScaledBitmap(square, 1024, 1024, true)
        } else {
            square
        }

        val avatarsDir = File(context.filesDir, "avatars").apply { mkdirs() }
        val outputFile = File(avatarsDir, "avatar_${System.currentTimeMillis()}.jpg")
        FileOutputStream(outputFile).use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }

        if (output !== square) output.recycle()
        square.recycle()
        bitmap.recycle()

        Uri.fromFile(outputFile).toString()
    }.getOrNull()
}

fun removeAvatarUri(context: Context, uriString: String?) {
    val uri = uriString?.let(Uri::parse) ?: return
    when (uri.scheme) {
        "file" -> {
            val filePath = uri.path ?: return
            runCatching { File(filePath).delete() }
        }
        "content" -> {
            // Legacy support for previously persisted content URIs.
            releaseReadPermission(context, uriString)
        }
    }
}


