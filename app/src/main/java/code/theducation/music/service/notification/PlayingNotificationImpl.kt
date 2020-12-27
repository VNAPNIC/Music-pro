package code.theducation.music.service.notification

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.text.HtmlCompat
import androidx.media.app.NotificationCompat.MediaStyle
import code.theducation.music.R
import code.theducation.music.activities.MainActivity
import code.theducation.music.db.PlaylistEntity
import code.theducation.music.db.toSongEntity
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.glide.palette.BitmapPaletteWrapper
import code.theducation.music.service.MusicService
import code.theducation.music.service.MusicService.*
import code.theducation.music.util.MusicUtil
import code.theducation.music.util.PreferenceUtil
import code.theducation.music.util.RetroColorUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

class PlayingNotificationImpl : PlayingNotification(), KoinComponent {
    private var target: Target<BitmapPaletteWrapper>? = null

    @Synchronized
    override fun update() {
        stopped = false
        GlobalScope.launch {
            val song = service.currentSong
            val playlist: PlaylistEntity? = MusicUtil.repository.favoritePlaylist()
            val isPlaying = service.isPlaying
            val isFavorite = if (playlist != null) {
                val songEntity = song.toSongEntity(playlist.playListId)
                MusicUtil.repository.isFavoriteSong(songEntity).isNotEmpty()
            } else false

            val playButtonResId =
                if (isPlaying) R.drawable.ic_pause_white_48dp else R.drawable.ic_play_arrow_white_48dp
            val favoriteResId =
                if (isFavorite) R.drawable.ic_favorite else R.drawable.ic_favorite_border

            val action = Intent(service, MainActivity::class.java)
            action.putExtra(MainActivity.EXPAND_PANEL, PreferenceUtil.isExpandPanel)
            action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val clickIntent =
                PendingIntent.getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT)

            val serviceName = ComponentName(service, MusicService::class.java)
            val intent = Intent(ACTION_QUIT)
            intent.component = serviceName
            val deleteIntent = PendingIntent.getService(service, 0, intent, 0)

            val bigNotificationImageSize = service.resources
                .getDimensionPixelSize(R.dimen.notification_big_image_size)
            service.runOnUiThread {
                if (target != null) {
                    Glide.clear(target)
                }
                target = SongGlideRequest.Builder.from(Glide.with(service), song)
                    .checkIgnoreMediaStore(service)
                    .generatePalette(service).build()
                    .centerCrop()
                    .into(object : SimpleTarget<BitmapPaletteWrapper>(
                        bigNotificationImageSize,
                        bigNotificationImageSize
                    ) {
                        override fun onResourceReady(
                            resource: BitmapPaletteWrapper,
                            glideAnimation: GlideAnimation<in BitmapPaletteWrapper>
                        ) {
                            update(
                                resource.bitmap,
                                RetroColorUtil.getColor(resource.palette, Color.TRANSPARENT)
                            )
                        }

                        override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                            super.onLoadFailed(e, errorDrawable)
                            update(null, Color.TRANSPARENT)
                        }

                        fun update(bitmap: Bitmap?, color: Int) {
                            var bitmapFinal = bitmap
                            if (bitmapFinal == null) {
                                bitmapFinal = BitmapFactory.decodeResource(
                                    service.resources,
                                    R.drawable.default_audio_art
                                )
                            }

                            val toggleFavorite = NotificationCompat.Action(
                                favoriteResId,
                                service.getString(R.string.action_toggle_favorite),
                                retrievePlaybackAction(TOGGLE_FAVORITE)
                            )
                            val playPauseAction = NotificationCompat.Action(
                                playButtonResId,
                                service.getString(R.string.action_play_pause),
                                retrievePlaybackAction(ACTION_TOGGLE_PAUSE)
                            )
                            val previousAction = NotificationCompat.Action(
                                R.drawable.ic_skip_previous_round_white_32dp,
                                service.getString(R.string.action_previous),
                                retrievePlaybackAction(ACTION_REWIND)
                            )
                            val nextAction = NotificationCompat.Action(
                                R.drawable.ic_skip_next_round_white_32dp,
                                service.getString(R.string.action_next),
                                retrievePlaybackAction(ACTION_SKIP)
                            )

                            val builder = NotificationCompat.Builder(
                                service,
                                NOTIFICATION_CHANNEL_ID
                            )
                                .setSmallIcon(R.drawable.ic_notification)
                                .setLargeIcon(bitmapFinal)
                                .setContentIntent(clickIntent)
                                .setDeleteIntent(deleteIntent)
                                .setContentTitle(
                                    HtmlCompat.fromHtml(
                                        "<b>" + song.title + "</b>",
                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                    )
                                )
                                .setContentText(song.artistName)
                                .setSubText(
                                    HtmlCompat.fromHtml(
                                        "<b>" + song.albumName + "</b>",
                                        HtmlCompat.FROM_HTML_MODE_LEGACY
                                    )
                                )
                                .setOngoing(isPlaying)
                                .setShowWhen(false)
                                .addAction(toggleFavorite)
                                .addAction(previousAction)
                                .addAction(playPauseAction)
                                .addAction(nextAction)

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                builder.setStyle(
                                    MediaStyle()
                                        .setMediaSession(service.mediaSession.sessionToken)
                                        .setShowActionsInCompactView(1, 2, 3)
                                )
                                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                                if (Build.VERSION.SDK_INT <=
                                    Build.VERSION_CODES.O && PreferenceUtil.isColoredNotification
                                ) {
                                    builder.color = color
                                }
                            }

                            if (stopped) {
                                return  // notification has been stopped before loading was finished
                            }
                            updateNotifyModeAndPostNotification(builder.build())
                        }
                    })
            }
        }
    }

    private fun retrievePlaybackAction(action: String): PendingIntent {
        val serviceName = ComponentName(service, MusicService::class.java)
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(service, 0, intent, 0)
    }
}