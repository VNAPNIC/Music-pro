package code.theducation.music.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.theducation.appthemehelper.ThemeStore
import code.theducation.appthemehelper.util.ColorUtil
import code.theducation.music.*
import code.theducation.music.adapter.album.AlbumAdapter
import code.theducation.music.adapter.artist.ArtistAdapter
import code.theducation.music.adapter.song.SongAdapter
import code.theducation.music.extensions.hide
import code.theducation.music.glide.SongGlideRequest
import code.theducation.music.helper.MusicPlayerRemote
import code.theducation.music.interfaces.IAlbumClickListener
import code.theducation.music.interfaces.IArtistClickListener
import code.theducation.music.interfaces.IGenreClickListener
import code.theducation.music.model.*
import code.theducation.music.util.PreferenceUtil
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView

class HomeAdapter(
    private val activity: AppCompatActivity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), IArtistClickListener, IAlbumClickListener,
    IGenreClickListener {

    private var list = listOf<Home>()

    override fun getItemViewType(position: Int): Int {
        return list[position].homeSection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout =
            LayoutInflater.from(activity).inflate(R.layout.section_recycler_view, parent, false)
        return when (viewType) {
            RECENT_ARTISTS, TOP_ARTISTS -> ArtistViewHolder(layout)
            GENRES -> GenreViewHolder(layout)
            FAVOURITES -> PlaylistViewHolder(layout)
            TOP_ALBUMS, RECENT_ALBUMS -> AlbumViewHolder(layout)
            else -> {
                SuggestionsViewHolder(
                    LayoutInflater.from(activity).inflate(
                        R.layout.item_suggestions,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val home = list[position]
        when (getItemViewType(position)) {
            RECENT_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ALBUMS)
                    )
                }
            }
            TOP_ALBUMS -> {
                val viewHolder = holder as AlbumViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ALBUMS)
                    )
                }
            }
            RECENT_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to RECENT_ARTISTS)
                    )
                }
            }
            TOP_ARTISTS -> {
                val viewHolder = holder as ArtistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to TOP_ARTISTS)
                    )
                }
            }
            SUGGESTIONS -> {
                val viewHolder = holder as SuggestionsViewHolder
                viewHolder.bindView(home)
            }
            FAVOURITES -> {
                val viewHolder = holder as PlaylistViewHolder
                viewHolder.bindView(home)
                viewHolder.clickableArea.setOnClickListener {
                    activity.findNavController(R.id.fragment_container).navigate(
                        R.id.detailListFragment,
                        bundleOf("type" to FAVOURITES)
                    )
                }
            }
            GENRES -> {
                val viewHolder = holder as GenreViewHolder
                viewHolder.bind(home)
            }
            PLAYLISTS -> {
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun swapData(sections: List<Home>) {
        list = sections
        notifyDataSetChanged()
    }

    private inner class AlbumViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                adapter = albumAdapter(home.arrayList as List<Album>)
                layoutManager = gridLayoutManager()
            }
        }
    }

    private inner class ArtistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                layoutManager = linearLayoutManager()
                adapter = artistsAdapter(home.arrayList as List<Artist>)
            }
        }
    }

    private inner class SuggestionsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val images = listOf(
            R.id.image1,
            R.id.image2,
            R.id.image3,
            R.id.image4,
            R.id.image5,
            R.id.image6,
            R.id.image7,
            R.id.image8
        )

        fun bindView(home: Home) {
            val color = ThemeStore.accentColor(activity)
            itemView.findViewById<TextView>(R.id.message).setTextColor(color)
            itemView.findViewById<MaterialCardView>(R.id.card6).apply {
                setCardBackgroundColor(ColorUtil.withAlpha(color, 0.12f))
            }
            images.forEachIndexed { index, id ->
                itemView.findViewById<View>(id).setOnClickListener {
                    MusicPlayerRemote.playNext(home.arrayList[index] as Song)
                }
                SongGlideRequest.Builder.from(Glide.with(activity), home.arrayList[index] as Song)
                    .asBitmap()
                    .build()
                    .into(itemView.findViewById(id))
            }
        }
    }

    private inner class PlaylistViewHolder(view: View) : AbsHomeViewItem(view) {
        fun bindView(home: Home) {
            title.setText(home.titleRes)
            recyclerView.apply {
                val songAdapter = SongAdapter(
                    activity,
                    home.arrayList as MutableList<Song>,
                    R.layout.item_album_card, null
                )
                layoutManager = linearLayoutManager()
                adapter = songAdapter
            }
        }
    }

    private inner class GenreViewHolder(itemView: View) : AbsHomeViewItem(itemView) {
        fun bind(home: Home) {
            arrow.hide()
            title.setText(home.titleRes)
            val genreAdapter = GenreAdapter(
                activity,
                home.arrayList as List<Genre>,
                R.layout.item_grid_genre,
                this@HomeAdapter
            )
            recyclerView.apply {
                layoutManager = GridLayoutManager(activity, 3, GridLayoutManager.HORIZONTAL, false)
                adapter = genreAdapter
            }
        }
    }

    open class AbsHomeViewItem(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        val title: AppCompatTextView = itemView.findViewById(R.id.title)
        val arrow: ImageView = itemView.findViewById(R.id.arrow)
        val clickableArea: ViewGroup = itemView.findViewById(R.id.clickable_area)
    }

    fun artistsAdapter(artists: List<Artist>) =
        ArtistAdapter(activity, artists, PreferenceUtil.homeArtistGridStyle, null, this)

    fun albumAdapter(albums: List<Album>) =
        AlbumAdapter(activity, albums, PreferenceUtil.homeAlbumGridStyle, null, this)

    fun gridLayoutManager() = GridLayoutManager(activity, 1, GridLayoutManager.HORIZONTAL, false)

    fun linearLayoutManager() = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)

    override fun onArtist(artistId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.artistDetailsFragment,
            bundleOf(EXTRA_ARTIST_ID to artistId),
            null,
            FragmentNavigatorExtras(
                view to "artist"
            )
        )
    }

    override fun onAlbumClick(albumId: Long, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.albumDetailsFragment,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,
            FragmentNavigatorExtras(
                view to "album"
            )
        )
    }

    override fun onClickGenre(genre: Genre, view: View) {
        activity.findNavController(R.id.fragment_container).navigate(
            R.id.genreDetailsFragment,
            bundleOf(EXTRA_GENRE to genre),
            null,
            FragmentNavigatorExtras(
                view to "genre"
            )
        )
    }

}