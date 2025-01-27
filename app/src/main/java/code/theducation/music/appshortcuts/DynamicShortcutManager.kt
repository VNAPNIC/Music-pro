package code.theducation.music.appshortcuts

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import code.theducation.music.appshortcuts.shortcuttype.LastAddedShortcutType
import code.theducation.music.appshortcuts.shortcuttype.ShuffleAllShortcutType
import code.theducation.music.appshortcuts.shortcuttype.TopTracksShortcutType

@TargetApi(Build.VERSION_CODES.N_MR1)
class DynamicShortcutManager(private val context: Context) {
    private val shortcutManager: ShortcutManager =
        this.context.getSystemService(ShortcutManager::class.java)

    private val defaultShortcuts: List<ShortcutInfo>
        get() = listOf(
            ShuffleAllShortcutType(context).shortcutInfo,
            TopTracksShortcutType(context).shortcutInfo,
            LastAddedShortcutType(context).shortcutInfo
        )

    fun initDynamicShortcuts() {
        // if (shortcutManager.dynamicShortcuts.size == 0) {
        shortcutManager.dynamicShortcuts = defaultShortcuts
        // }
    }

    fun updateDynamicShortcuts() {
        shortcutManager.updateShortcuts(defaultShortcuts)
    }

    companion object {

        fun createShortcut(
            context: Context,
            id: String,
            shortLabel: String,
            longLabel: String,
            icon: Icon,
            intent: Intent
        ): ShortcutInfo {
            return ShortcutInfo.Builder(context, id)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(icon)
                .setIntent(intent)
                .build()
        }

        fun reportShortcutUsed(context: Context, shortcutId: String) {
            context.getSystemService(ShortcutManager::class.java).reportShortcutUsed(shortcutId)
        }
    }
}
