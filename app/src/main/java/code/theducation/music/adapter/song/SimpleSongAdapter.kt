package code.theducation.music.adapter.song

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import code.theducation.music.interfaces.ICabHolder
import code.theducation.music.model.Song
import code.theducation.music.util.MusicUtil
import java.util.*

class SimpleSongAdapter(
    context: FragmentActivity,
    songs: ArrayList<Song>,
    layoutRes: Int,
    ICabHolder: ICabHolder?
) : SongAdapter(context, songs, layoutRes, ICabHolder) {

    override fun swapDataSet(dataSet: List<Song>) {
        this.dataSet = dataSet.toMutableList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val fixedTrackNumber = MusicUtil.getFixedTrackNumber(dataSet[position].trackNumber)
        val trackAndTime = (if (fixedTrackNumber > 0) "$fixedTrackNumber | " else "") +
                MusicUtil.getReadableDurationString(dataSet[position].duration)

        holder.imageText?.visibility = View.GONE
        holder.time?.text = trackAndTime
        holder.text2?.text = dataSet[position].artistName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}
