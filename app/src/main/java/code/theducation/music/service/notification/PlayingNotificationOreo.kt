package code.theducation.music.service.notification

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import code.theducation.appthemehelper.util.ATHUtil.resolveColor
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.appthemehelper.util.MaterialValueHelper
import code.theducation.music.R
import code.theducation.music.activities.MainActivity
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.glide.palette.BitmapPaletteWrapper
import code.theducation.music.model.Song
import code.theducation.music.service.MusicService
import code.theducation.music.service.MusicService.*
import code.theducation.music.util.PreferenceUtil
import code.theducation.music.util.Utils
import code.theducation.music.util.Utils.createBitmap
import code.theducation.music.util.color.MediaNotificationProcessor
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target

/**
 * @author nankai
 */
class PlayingNotificationOreo : PlayingNotification() {

    private var target: Target<BitmapPaletteWrapper>? = null

    private fun getCombinedRemoteViews(collapsed: Boolean, song: Song): RemoteViews {
        val remoteViews = RemoteViews(
            service.packageName,
            if (collapsed) R.layout.layout_notification_collapsed else R.layout.layout_notification_expanded
        )
        remoteViews.setTextViewText(
            R.id.appName,
            service.getString(R.string.app_name) + " • " + song.albumName
        )
        remoteViews.setTextViewText(R.id.title, song.title)
        remoteViews.setTextViewText(R.id.subtitle, song.artistName)
        linkButtons(remoteViews)
        return remoteViews
    }

    override fun update() {
        stopped = false
        val song = service.currentSong
        val isPlaying = service.isPlaying

        val notificationLayout = getCombinedRemoteViews(true, song)
        val notificationLayoutBig = getCombinedRemoteViews(false, song)

        val action = Intent(service, MainActivity::class.java)
        action.putExtra(MainActivity.EXPAND_PANEL, PreferenceUtil.isExpandPanel)
        action.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

        val clickIntent = PendingIntent
            .getActivity(service, 0, action, PendingIntent.FLAG_UPDATE_CURRENT)
        val deleteIntent = buildPendingIntent(service, ACTION_QUIT, null)

        val builder = NotificationCompat.Builder(service, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(clickIntent)
            .setDeleteIntent(deleteIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCustomContentView(notificationLayout)
            .setCustomBigContentView(notificationLayoutBig)
            .setOngoing(isPlaying)

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
                        /* val mediaNotificationProcessor = MediaNotificationProcessor(
                             service,
                             service
                         ) { i, _ -> update(resource.bitmap, i) }
                         mediaNotificationProcessor.processNotification(resource.bitmap)*/

                        val colors = MediaNotificationProcessor(service, resource.bitmap)
                        update(resource.bitmap, colors.backgroundColor)
                    }

                    override fun onLoadFailed(e: Exception?, errorDrawable: Drawable?) {
                        super.onLoadFailed(e, errorDrawable)
                        update(
                            null,
                            resolveColor(service, R.attr.colorSurface, Color.WHITE)
                        )
                    }

                    private fun update(bitmap: Bitmap?, bgColor: Int) {
                        var bgColorFinal = bgColor
                        if (bitmap != null) {
                            notificationLayout.setImageViewBitmap(R.id.largeIcon, bitmap)
                            notificationLayoutBig.setImageViewBitmap(R.id.largeIcon, bitmap)
                        } else {
                            notificationLayout.setImageViewResource(
                                R.id.largeIcon,
                                R.drawable.default_audio_art
                            )
                            notificationLayoutBig.setImageViewResource(
                                R.id.largeIcon,
                                R.drawable.default_audio_art
                            )
                        }

                        if (!PreferenceUtil.isColoredNotification) {
                            bgColorFinal = resolveColor(service, R.attr.colorPrimary, Color.WHITE)
                        }
                        setBackgroundColor(bgColorFinal)
                        setNotificationContent(ColorUtil.isColorLight(bgColorFinal))

                        if (stopped) {
                            return  // notification has been stopped before loading was finished
                        }
                        updateNotifyModeAndPostNotification(builder.build())
                    }

                    private fun setBackgroundColor(color: Int) {
                        notificationLayout.setInt(R.id.image, "setBackgroundColor", color)
                        notificationLayoutBig.setInt(R.id.image, "setBackgroundColor", color)
                    }

                    private fun setNotificationContent(dark: Boolean) {
                        val primary = MaterialValueHelper.getPrimaryTextColor(service, dark)
                        val secondary = MaterialValueHelper.getSecondaryTextColor(service, dark)

                        val close = createBitmap(
                            Utils.getTintedVectorDrawable(
                                service,
                                R.drawable.ic_close,
                                primary
                            )!!, NOTIFICATION_CONTROLS_SIZE_MULTIPLIER
                        )
                        val prev = createBitmap(
                            Utils.getTintedVectorDrawable(
                                service,
                                R.drawable.ic_skip_previous_round_white_32dp,
                                primary
                            )!!, NOTIFICATION_CONTROLS_SIZE_MULTIPLIER
                        )
                        val next = createBitmap(
                            Utils.getTintedVectorDrawable(
                                service,
                                R.drawable.ic_skip_next_round_white_32dp,
                                primary
                            )!!, NOTIFICATION_CONTROLS_SIZE_MULTIPLIER
                        )
                        val playPause = createBitmap(
                            Utils.getTintedVectorDrawable(
                                service,
                                if (isPlaying)
                                    R.drawable.ic_pause_white_48dp
                                else
                                    R.drawable.ic_play_arrow_white_48dp, primary
                            )!!, NOTIFICATION_CONTROLS_SIZE_MULTIPLIER
                        )

                        notificationLayout.setTextColor(R.id.title, primary)
                        notificationLayout.setTextColor(R.id.subtitle, secondary)
                        notificationLayout.setTextColor(R.id.appName, secondary)

                        notificationLayout.setImageViewBitmap(R.id.action_prev, prev)
                        notificationLayout.setImageViewBitmap(R.id.action_next, next)
                        notificationLayout.setImageViewBitmap(R.id.action_play_pause, playPause)

                        notificationLayoutBig.setTextColor(R.id.title, primary)
                        notificationLayoutBig.setTextColor(R.id.subtitle, secondary)
                        notificationLayoutBig.setTextColor(R.id.appName, secondary)

                        notificationLayoutBig.setImageViewBitmap(R.id.action_quit, close)
                        notificationLayoutBig.setImageViewBitmap(R.id.action_prev, prev)
                        notificationLayoutBig.setImageViewBitmap(R.id.action_next, next)
                        notificationLayoutBig.setImageViewBitmap(R.id.action_play_pause, playPause)

                        notificationLayout.setImageViewBitmap(
                            R.id.smallIcon,
                            createBitmap(
                                Utils.getTintedVectorDrawable(
                                    service,
                                    R.drawable.ic_notification,
                                    secondary
                                )!!, 0.6f
                            )
                        )
                        notificationLayoutBig.setImageViewBitmap(
                            R.id.smallIcon,
                            createBitmap(
                                Utils.getTintedVectorDrawable(
                                    service,
                                    R.drawable.ic_notification,
                                    secondary
                                )!!, 0.6f
                            )
                        )

                    }
                })
        }

        if (stopped) {
            return  // notification has been stopped before loading was finished
        }
        updateNotifyModeAndPostNotification(builder.build())
    }


    private fun buildPendingIntent(
        context: Context, action: String,
        serviceName: ComponentName?
    ): PendingIntent {
        val intent = Intent(action)
        intent.component = serviceName
        return PendingIntent.getService(context, 0, intent, 0)
    }


    private fun linkButtons(notificationLayout: RemoteViews) {
        var pendingIntent: PendingIntent

        val serviceName = ComponentName(service, MusicService::class.java)

        // Previous track
        pendingIntent = buildPendingIntent(service, ACTION_REWIND, serviceName)
        notificationLayout.setOnClickPendingIntent(R.id.action_prev, pendingIntent)

        // Play and pause
        pendingIntent = buildPendingIntent(service, ACTION_TOGGLE_PAUSE, serviceName)
        notificationLayout.setOnClickPendingIntent(R.id.action_play_pause, pendingIntent)

        // Next track
        pendingIntent = buildPendingIntent(service, ACTION_SKIP, serviceName)
        notificationLayout.setOnClickPendingIntent(R.id.action_next, pendingIntent)

        // Close
        pendingIntent = buildPendingIntent(service, ACTION_QUIT, serviceName)
        notificationLayout.setOnClickPendingIntent(R.id.action_quit, pendingIntent)
    }

}
