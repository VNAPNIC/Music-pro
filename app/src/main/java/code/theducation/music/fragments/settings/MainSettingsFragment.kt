package code.theducation.music.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import code.theducation.music.R
import kotlinx.android.synthetic.main.fragment_main_settings.*

class MainSettingsFragment : Fragment(), View.OnClickListener {
    override fun onClick(view: View) {
        when (view.id) {
            R.id.generalSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_themeSettingsFragment)
            R.id.audioSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_audioSettings)
            R.id.personalizeSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_personalizeSettingsFragment)
            R.id.imageSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_imageSettingFragment)
            R.id.notificationSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_notificationSettingsFragment)
            R.id.otherSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_otherSettingsFragment)
            R.id.aboutSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_aboutActivity)
            R.id.nowPlayingSettings -> findNavController().navigate(R.id.action_mainSettingsFragment_to_nowPlayingSettingsFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generalSettings.setOnClickListener(this)
        audioSettings.setOnClickListener(this)
        nowPlayingSettings.setOnClickListener(this)
        personalizeSettings.setOnClickListener(this)
        imageSettings.setOnClickListener(this)
        notificationSettings.setOnClickListener(this)
        otherSettings.setOnClickListener(this)
        aboutSettings.setOnClickListener(this)
    }
}
