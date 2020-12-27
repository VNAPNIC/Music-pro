package code.theducation.music.fragments.settings

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import code.theducation.appthemehelper.common.prefs.supportv7.ATEListPreference
import code.theducation.music.LANGUAGE_NAME
import code.theducation.music.LAST_ADDED_CUTOFF
import code.theducation.music.R
import code.theducation.music.fragments.LibraryViewModel
import code.theducation.music.fragments.ReloadType.HomeSections
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

/**
 * @author nankai
 */

class OtherSettingsFragment : AbsSettingsFragment() {
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    override fun invalidateSettings() {
        val languagePreference: ATEListPreference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { _, _ ->
            requireActivity().recreate()
            return@setOnPreferenceChangeListener true
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_advanced)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val preference: Preference? = findPreference(LAST_ADDED_CUTOFF)
        preference?.setOnPreferenceChangeListener { lastAdded, newValue ->
            setSummary(lastAdded, newValue)
            libraryViewModel.forceReload(HomeSections)
            true
        }
        val languagePreference: Preference? = findPreference(LANGUAGE_NAME)
        languagePreference?.setOnPreferenceChangeListener { prefs, newValue ->
            setSummary(prefs, newValue)
            true
        }
    }
}
