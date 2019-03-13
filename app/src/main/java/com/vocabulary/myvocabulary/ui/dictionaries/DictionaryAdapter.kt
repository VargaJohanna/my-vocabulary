package com.vocabulary.myvocabulary.ui.dictionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.displayed
import com.vocabulary.myvocabulary.ext.show
import com.vocabulary.myvocabulary.utils.DictionaryDiffUtilCallBack
import kotlinx.android.synthetic.main.row_dictionary.view.*

class DictionaryAdapter(private var dictionaryList: List<Dictionary>,
                        private val itemClickListener: ItemClickListener,
                        private val allowItemEditing: Boolean) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return DictionaryViewHolder(inflater.inflate(R.layout.row_dictionary, parent, false))
    }

    override fun getItemCount(): Int {
        return dictionaryList.size
    }

    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
        holder.bind(dictionaryList[position])

    }

    inner class DictionaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(dictionary: Dictionary) {
            itemView.dictionary_name.text = dictionary.dictionaryName
            itemView.dictionary_options.displayed(allowItemEditing)
        }

        init {
            itemView.dictionary_name.setOnClickListener {
                itemClickListener.onItemClick(dictionaryList[adapterPosition])
            }

            itemView.dictionary_options.setOnClickListener {
                itemClickListener.onOptionsClick(dictionaryList[adapterPosition], it)
            }
        }
    }

    fun updateList(newList: List<Dictionary>) {
        val diffResult = DiffUtil.calculateDiff(DictionaryDiffUtilCallBack(dictionaryList, newList))
        this.dictionaryList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    interface ItemClickListener {
        fun onItemClick(dictionary: Dictionary)
        fun onOptionsClick(dictionary: Dictionary, view: View)
    }
}