package com.vocabulary.myvocabulary.ui.dictionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.row_dictionary.view.*

class DictionaryAdapter(private var dictionaryList: List<Dictionary>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {
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

    inner class DictionaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        fun bind(dictionary: Dictionary) {
            itemView.dictionary_name.text = dictionary.dictionaryName
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            itemClickListener.onItemClick(dictionaryList[adapterPosition])
        }
    }

    fun updateList(list: List<Dictionary>) {
        dictionaryList = list
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClick(dictionary: Dictionary)
    }
}