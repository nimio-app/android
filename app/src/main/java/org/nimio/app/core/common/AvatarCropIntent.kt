package org.nimio.app.core.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File

fun createSquareCropIntent(
    context: Context,
    sourceUri: Uri
): Intent {
    val outputFile = File(context.cacheDir, "crop_${System.currentTimeMillis()}.jpg")
    val destinationUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        outputFile
    )

    val options = UCrop.Options().apply {
        setHideBottomControls(false)
        setFreeStyleCropEnabled(false)
        setCompressionQuality(92)
        setMaxScaleMultiplier(6f)
    }

    return UCrop.of(sourceUri, destinationUri)
        .withAspectRatio(1f, 1f)
        .withMaxResultSize(1024, 1024)
        .withOptions(options)
        .getIntent(context)
}

