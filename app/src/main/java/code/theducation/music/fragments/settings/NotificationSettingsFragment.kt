package code.theducation.music.fragments.settings

import android.content.SharedPreferences
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.TwoStatePreference
import code.theducation.music.CLASSIC_NOTIFICATION
import code.theducation.music.COLORED_NOTIFICATION
import code.theducation.music.R
import code.theducation.music.util.PreferenceUtil

/**
 * @author nankai
 */

class NotificationSettingsFragment : AbsSettingsFragment(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == CLASSIC_NOTIFICATION) {
            if (VERSION.SDK_INT >= VERSION_CODES.O) {
                findPreference<Preference>(COLORED_NOTIFICATION)?.isEnabled =
                    sharedPreferences?.getBoolean(key, false)!!
            }
        }
    }

    override fun invalidateSettings() {

        val classicNotification: TwoStatePreference? = findPreference(CLASSIC_NOTIFICATION)
        if (VERSION.SDK_INT < VERSION_CODES.N) {
            classicNotification?.isVisible = false
        } else {
            classicNotification?.apply {
                isChecked = PreferenceUtil.isClassicNotification
                setOnPreferenceChangeListener { _, newValue ->
                    // Save preference
                    PreferenceUtil.isClassicNotification = newValue as Boolean
                    invalidateSettings()
                    true
                }
            }
        }

        val coloredNotification: TwoStatePreference? = findPreference(COLORED_NOTIFICATION)
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            coloredNotification?.isEnabled = PreferenceUtil.isClassicNotification
        } else {
            coloredNotification?.apply {
                isChecked = PreferenceUtil.isColoredNotification
                setOnPreferenceChangeListener { _, newValue ->
                    PreferenceUtil.isColoredNotification = newValue as Boolean
                    true
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_notification)
    }
}
