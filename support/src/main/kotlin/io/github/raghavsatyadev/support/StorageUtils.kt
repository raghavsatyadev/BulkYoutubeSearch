package io.github.raghavsatyadev.support

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import io.github.raghavsatyadev.support.core.CoreApp
import io.github.raghavsatyadev.support.extensions.AppExtensions.kotlinFileName
import java.io.File
import java.io.IOException

@Suppress("unused")
object StorageUtils {
    fun createInternalDirectory(): String {
        return CoreApp.instance.filesDir.absolutePath
    }

    fun createFile(
        parentFolderName: String? = null,
        fileName: String,
        replace: Boolean = false,
    ): File {
        val file = getFilePathWithoutCreating(fileName, parentFolderName, replace)
        try {
            if (replace || !file.exists()) file.createNewFile()
        } catch (e: IOException) {
            AppLog.loge(
                false,
                kotlinFileName,
                "createFile",
                e,
                Exception()
            )
        }
        return file
    }

    fun File.getUriForFile(context: Context): Uri {
        return FileProvider.getUriForFile(
            context,
            CoreApp.instance.packageName + ".fileprovider",
            this
        )
    }

    /**
     * Create a file in the storage location taken from the
     * [StorageUtils.createExternalDirectory]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getFilePathWithoutCreating(
        fileName: String,
        parentFolderName: String? = null,
        replace: Boolean = false,
    ): File {
        val root = createExternalDirectory()
        val myDir = if (!parentFolderName.isNullOrEmpty()) {
            File(root, parentFolderName)
        } else {
            File(root)
        }
        if (!myDir.exists()) {
            myDir.mkdirs()
        }
        var file = myDir
        if (fileName.isNotEmpty()) {
            file = File(myDir, fileName)
            if (file.exists()) {
                if (replace) file.delete()
            }
        }
        return file
    }

    fun getCacheDirectory(): File {
        return CoreApp.instance.cacheDir
    }

    /**
     * tries to create external file directory in downloads with app name, if
     * not possible it will create internal folder
     */
    private fun createExternalDirectory(): String {
        val state = Environment.getExternalStorageState()
        var baseDir: String? = null
        if (Environment.MEDIA_MOUNTED == state) {
            val baseDirFile: File? = CoreApp.instance.getExternalFilesDir(
                Environment.DIRECTORY_DOWNLOADS
            )
            if (baseDirFile != null) {
                baseDir = baseDirFile.absolutePath
            }
        }
        if (baseDir == null) {
            baseDir = CoreApp.instance.filesDir.absolutePath
        }
        return baseDir!!
    }
}