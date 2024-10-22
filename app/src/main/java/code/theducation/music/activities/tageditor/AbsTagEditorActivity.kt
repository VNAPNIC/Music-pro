package code.theducation.music.activities.tageditor

import android.app.Activity
import android.app.SearchManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AlertDialog
import code.theducation.appthemehelper.ThemeStore
import code.theducation.appthemehelper.util.ATHUtil
import code.theducation.appthemehelper.util.TintHelper
import code.theducation.music.R
import code.theducation.music.R.drawable
import code.theducation.music.activities.base.AbsBaseActivity
import code.theducation.music.activities.saf.SAFGuideActivity
import code.theducation.music.extensions.accentColor
import code.theducation.music.model.ArtworkInfo
import code.theducation.music.model.LoadingInfo
import code.theducation.music.repository.Repository
import code.theducation.music.util.Utils
import code.theducation.music.util.SAFUtil
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_album_tag_editor.*
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.koin.android.ext.android.inject
import java.io.File
import java.util.*

abstract class AbsTagEditorActivity : AbsBaseActivity() {
    val repository by inject<Repository>()

    lateinit var saveFab: MaterialButton
    protected var id: Long = 0
        private set
    private var paletteColorPrimary: Int = 0
    private var isInNoImageMode: Boolean = false
    private var songPaths: List<String>? = null
    private var savedSongPaths: List<String>? = null
    private val currentSongPath: String? = null
    private var savedTags: Map<FieldKey, String>? = null
    private var savedArtworkInfo: ArtworkInfo? = null
    protected abstract val contentViewLayout: Int
    protected abstract fun loadImageFromFile(selectedFile: Uri?)

    protected val show: AlertDialog
        get() =
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.update_image)
                .setItems(items.toTypedArray()) { _, position ->
                    when (position) {
                        0 -> startImagePicker()
                        1 -> searchImageOnWeb()
                        2 -> deleteImage()
                    }
                }
                .show()

    internal val albumArtist: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.ALBUM_ARTIST)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val songTitle: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.TITLE)
            } catch (ignored: Exception) {
                null
            }
        }
    protected val composer: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.COMPOSER)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val albumTitle: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.ALBUM)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val artistName: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.ARTIST)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val albumArtistName: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.ALBUM_ARTIST)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val genreName: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.GENRE)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val songYear: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.YEAR)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val trackNumber: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.TRACK)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val lyrics: String?
        get() {
            return try {
                getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.getFirst(FieldKey.LYRICS)
            } catch (ignored: Exception) {
                null
            }
        }

    protected val albumArt: Bitmap?
        get() {
            try {
                val artworkTag = getAudioFile(songPaths!![0]).tagOrCreateAndSetDefault.firstArtwork
                if (artworkTag != null) {
                    val artworkBinaryData = artworkTag.binaryData
                    return BitmapFactory.decodeByteArray(
                        artworkBinaryData,
                        0,
                        artworkBinaryData.size
                    )
                }
                return null
            } catch (ignored: Exception) {
                return null
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewLayout)
        setStatusbarColorAuto()
        setNavigationbarColorAuto()
        setTaskDescriptionColorAuto()

        saveFab = findViewById(R.id.saveTags)
        getIntentExtras()

        songPaths = getSongPaths()
        println(songPaths?.size)
        if (songPaths!!.isEmpty()) {
            finish()
        }
        setUpViews()
    }

    private fun setUpViews() {
        setUpFab()
        setUpImageView()
    }

    private lateinit var items: List<String>

    private fun setUpImageView() {
        loadCurrentImage()
        items = listOf(
            getString(R.string.pick_from_local_storage),
            getString(R.string.web_search),
            getString(R.string.remove_cover)
        )
        editorImage?.setOnClickListener { show }
    }

    private fun startImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(
            Intent.createChooser(
                intent,
                getString(R.string.pick_from_local_storage)
            ), REQUEST_CODE_SELECT_IMAGE
        )
    }

    protected abstract fun loadCurrentImage()

    protected abstract fun searchImageOnWeb()

    protected abstract fun deleteImage()

    private fun setUpFab() {
        saveFab.accentColor()
        saveFab.apply {
            scaleX = 0f
            scaleY = 0f
            isEnabled = false
            setOnClickListener { save() }
            TintHelper.setTintAuto(this, ThemeStore.accentColor(this@AbsTagEditorActivity), true)
        }
    }

    protected abstract fun save()

    private fun getIntentExtras() {
        val intentExtras = intent.extras
        if (intentExtras != null) {
            id = intentExtras.getLong(EXTRA_ID)
        }
    }

    protected abstract fun getSongPaths(): List<String>

    protected fun searchWebFor(vararg keys: String) {
        val stringBuilder = StringBuilder()
        for (key in keys) {
            stringBuilder.append(key)
            stringBuilder.append(" ")
        }
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun setNoImageMode() {
        isInNoImageMode = true
        imageContainer?.visibility = View.GONE
        editorImage?.visibility = View.GONE
        editorImage?.isEnabled = false

        setColors(
            intent.getIntExtra(
                EXTRA_PALETTE,
                ATHUtil.resolveColor(this, R.attr.colorPrimary)
            )
        )
    }

    protected fun dataChanged() {
        showFab()
    }

    private fun showFab() {
        saveFab.animate().setDuration(500).setInterpolator(OvershootInterpolator()).scaleX(1f)
            .scaleY(1f).start()
        saveFab.isEnabled = true
    }

    private fun hideFab() {
        saveFab.animate().setDuration(500).setInterpolator(OvershootInterpolator()).scaleX(0.0f)
            .scaleY(0.0f).start()
        saveFab.isEnabled = false
    }

    protected fun setImageBitmap(bitmap: Bitmap?, bgColor: Int) {
        if (bitmap == null) {
            editorImage.setImageResource(drawable.default_audio_art)
        } else {
            editorImage.setImageBitmap(bitmap)
        }
        setColors(bgColor)
    }

    protected open fun setColors(color: Int) {
        paletteColorPrimary = color
    }

    protected fun writeValuesToFiles(
        fieldKeyValueMap: Map<FieldKey, String>,
        artworkInfo: ArtworkInfo?
    ) {
        Utils.hideSoftKeyboard(this)

        hideFab()
        println(fieldKeyValueMap)
        WriteTagsAsyncTask(this).execute(
            LoadingInfo(
                songPaths,
                fieldKeyValueMap,
                artworkInfo
            )
        )
    }

    private fun writeTags(paths: List<String>?) {
        WriteTagsAsyncTask(this).execute(
            LoadingInfo(
                paths,
                savedTags,
                savedArtworkInfo
            )
        )
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                intent?.data?.let {
                    loadImageFromFile(it)
                }
            }
            SAFGuideActivity.REQUEST_CODE_SAF_GUIDE -> {
                SAFUtil.openTreePicker(this)
            }
            SAFUtil.REQUEST_SAF_PICK_TREE -> {
                if (resultCode == Activity.RESULT_OK) {
                    SAFUtil.saveTreeUri(this, intent)
                    writeTags(savedSongPaths)
                }
            }
            SAFUtil.REQUEST_SAF_PICK_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    writeTags(Collections.singletonList(currentSongPath + SAFUtil.SEPARATOR + intent!!.dataString))
                }
            }
        }
    }


    private fun getAudioFile(path: String): AudioFile {
        return try {
            AudioFileIO.read(File(path))
        } catch (e: Exception) {
            Log.e(TAG, "Could not read audio file $path", e)
            AudioFile()
        }
    }


    companion object {

        const val EXTRA_ID = "extra_id"
        const val EXTRA_PALETTE = "extra_palette"
        private val TAG = AbsTagEditorActivity::class.java.simpleName
        private const val REQUEST_CODE_SELECT_IMAGE = 1000
    }
}
