package code.theducation.music.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import code.theducation.music.R
import code.theducation.music.extensions.colorButtons
import code.theducation.music.extensions.materialDialog
import code.theducation.music.fragments.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ImportPlaylistDialog : DialogFragment() {
    private val libraryViewModel by sharedViewModel<LibraryViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return materialDialog(R.string.import_playlist)
            .setMessage(R.string.import_playlist_message)
            .setPositiveButton(R.string.import_label) { _, _ ->
                libraryViewModel.importPlaylists()
            }
            .create()
            .colorButtons()
    }
}
