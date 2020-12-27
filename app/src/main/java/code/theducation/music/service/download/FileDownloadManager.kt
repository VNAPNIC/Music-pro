package code.theducation.music.service.download

import android.content.Context
import android.content.Intent
import java.util.ArrayList

object FileDownloadManager {
    // file path is folder path
    // file name must be given with extension
    var indexToDownload: Int = 0
    var listOfFilesToBeDownloaded: ArrayList<FileToDownload?>? = ArrayList()
    var isNotificationShowing = false

    // calling start service again doesnot create new instance if service is already running
    fun initDownload(
        context: Context,
        fileDownload: FileToDownload
    ) {
        listOfFilesToBeDownloaded?.add(fileDownload)
        isNotificationShowing = listOfFilesToBeDownloaded?.size ?: 0 > 1
        val intent = Intent(context, DownloadService::class.java)
        context.startService(intent)
    }
}