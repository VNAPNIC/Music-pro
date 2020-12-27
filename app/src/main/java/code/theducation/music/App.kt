package code.theducation.music

import androidx.multidex.MultiDexApplication
import code.theducation.appthemehelper.ThemeStore
import code.theducation.appthemehelper.util.VersionUtils
import code.theducation.music.appshortcuts.DynamicShortcutManager
import com.google.android.gms.ads.InterstitialAd
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin


class App : MultiDexApplication() {
    lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }
        // default theme
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this)
                .accentColorRes(R.color.md_deep_purple_A200)
                .coloredNavigationBar(true)
                .commit()
        }

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }
    }
}
