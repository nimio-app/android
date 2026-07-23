package org.nimio.app.core.common

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri

fun persistReadPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

fun releaseReadPermission(context: Context, uriString: String?) {
    val uri = uriString?.let(Uri::parse) ?: return
    runCatching {
        context.contentResolver.releasePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
}

