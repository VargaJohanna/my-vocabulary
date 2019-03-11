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
            itemView.findViewById<TextView>(R.id.dictionary_name).setOnClickListener(this)
            itemView.findViewById<TextView>(R.id.dictionary_options).setOnClickListener(this)
        }

        override fun onClick(view: View) {
            itemClickListener.onItemClick(dictionaryList[adapterPosition], view)
        }
    }

    fun updateList(list: List<Dictionary>) {
        dictionaryList = list
        notifyDataSetChanged()
    }

    interface ItemClickListener {
        fun onItemClick(dictionary: Dictionary, view: View)
    }
}