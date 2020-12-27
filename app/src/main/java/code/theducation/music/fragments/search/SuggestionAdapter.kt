package code.theducation.music.fragments.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code.theducation.music.R
import kotlinx.android.synthetic.main.item_suggestion.view.*

interface SuggestionCallback {
    fun applySuggestion(suggestion: String)
    fun onSearching(suggestion: String)
}

class SuggestionAdapter(val callback: SuggestionCallback) :
    RecyclerView.Adapter<SuggestionAdapter.SearchSuggestionViewHolder>() {
    private var suggestions = arrayListOf<String>()
    fun addNew(suggestions: ArrayList<String>) {
        this.suggestions = suggestions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchSuggestionViewHolder {
        return SearchSuggestionViewHolder(
            callback,
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_suggestion, parent, false)
        )
    }

    override fun getItemCount(): Int = suggestions.size

    override fun onBindViewHolder(holder: SearchSuggestionViewHolder, position: Int) {
        holder.onBin(suggestions[position])
    }

    inner class SearchSuggestionViewHolder(val callback: SuggestionCallback, val view: View) :
        RecyclerView.ViewHolder(view) {
        fun onBin(suggestion: String) {
            try {
                view.suggestionIcon.setImageResource(R.drawable.abc_ic_search_api_material)
                view.title.text = suggestion
                view.imgSuggestion.setOnClickListener {
                    callback.applySuggestion(suggestion)
                }
                view.setOnClickListener {
                    callback.onSearching(suggestion)
                }
            } catch (e: Exception) {
            }
        }
    }
}