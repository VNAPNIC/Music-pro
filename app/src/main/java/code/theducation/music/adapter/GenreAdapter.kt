package code.theducation.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import code.theducation.music.R
import code.theducation.music.adapter.base.MediaEntryViewHolder
import code.theducation.music.interfaces.IGenreClickListener
import code.theducation.music.model.Genre
import java.util.*

/**
 * @author nankai
 */

class GenreAdapter(
    private val activity: FragmentActivity,
    var dataSet: List<Genre>,
    private val mItemLayoutRes: Int,
    private val listener: IGenreClickListener
) : RecyclerView.Adapter<GenreAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(mItemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = dataSet[position]
        holder.title?.text = genre.name
        holder.text?.text = String.format(
            Locale.getDefault(),
            "%d %s",
            genre.songCount,
            if (genre.songCount > 1) activity.getString(R.string.songs) else activity.getString(R.string.song)
        )
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun swapDataSet(list: List<Genre>) {
        dataSet = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : MediaEntryViewHolder(itemView) {
        override fun onClick(v: View?) {
            ViewCompat.setTransitionName(itemView, "genre")
            listener.onClickGenre(dataSet[layoutPosition], itemView)
        }
    }
}
