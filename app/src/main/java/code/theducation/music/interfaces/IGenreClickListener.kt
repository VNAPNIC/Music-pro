package code.theducation.music.interfaces

import android.view.View
import code.theducation.music.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}