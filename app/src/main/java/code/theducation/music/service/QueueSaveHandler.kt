package code.theducation.music.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import code.theducation.music.service.MusicService.SAVE_QUEUES
import java.lang.ref.WeakReference

internal class QueueSaveHandler(
    musicService: MusicService,
    looper: Looper
) : Handler(looper) {
    private val service: WeakReference<MusicService> = WeakReference(musicService)

    override fun handleMessage(msg: Message) {
        val service: MusicService? = service.get()
        if (msg.what == SAVE_QUEUES) {
            service?.saveQueuesImpl()
        }
    }
}
