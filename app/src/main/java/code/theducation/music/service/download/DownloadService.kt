package code.theducation.music.service.download

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.IBinder
import code.theducation.music.util.MusicUtil
import kotlinx.coroutines.*

import kotlin.collections.ArrayList

class DownloadService : Service() {

    private lateinit var downloadManager: DownloadManager

    override fun onCreate() {
        super.onCreate()
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        initDownload()
        return START_STICKY
    }

    private fun initDownload() {
        val list = FileDownloadManager.listOfFilesToBeDownloaded
        if (list != null && list.size > 0) {
            if (FileDownloadManager.indexToDownload < list.size) {
                val fileToDownload = list[FileDownloadManager.indexToDownload]
                if (fileToDownload != null && !fileToDownload.isDownloaded) {
                    fileToDownload.isDownloaded = true
                    download(fileToDownload)
                }
            }
        }
    }

    private val scopeIO = CoroutineScope(Dispatchers.IO)
    fun download(fileToDownload: FileToDownload) = runBlocking { // this: CoroutineScope
        launch {
            try {
                val uri = Uri.parse(fileToDownload.song.data)
                val request = DownloadManager.Request(uri).apply {
                    setAllowedNetworkTypes(
                        DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
                    )
                    setAllowedOverRoaming(false)
                    setTitle(fileToDownload.song.title)
                    setMimeType(fileToDownload.song.mimeType)
                    setDescription("Downloading...")

                    setDestinationUri(
                        Uri.fromFile(
                            MusicUtil.mediaFile(
                                fileToDownload.song.title,
                                fileToDownload.song.defaultExt!!
                            )
                        )
                    )

                    setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                }
                val downloadID = downloadManager.enqueue(request)
                val onDownloadComplete: BroadcastReceiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                        if (downloadID == id) {
                            val newIndexToDownload = FileDownloadManager.indexToDownload + 1
                            if (newIndexToDownload < FileDownloadManager.listOfFilesToBeDownloaded?.size!!) {
                                FileDownloadManager.indexToDownload = newIndexToDownload
                                initDownload()
                                //Unregister this broadcast Receiver
                                this@DownloadService.unregisterReceiver(this)
                            } else {
                                FileDownloadManager.indexToDownload = 0
                                FileDownloadManager.listOfFilesToBeDownloaded = ArrayList()
                                //Unregister this broadcast Receiver
                                this@DownloadService.unregisterReceiver(this)
                                stopSelf()
                                scopeIO.cancel()
                            }
                        }
                    }
                }
                registerReceiver(
                    onDownloadComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // when any error occurs stop the service by calling stop self
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        FileDownloadManager.indexToDownload = 0
        FileDownloadManager.listOfFilesToBeDownloaded = ArrayList()
        stopSelf()
    }
}