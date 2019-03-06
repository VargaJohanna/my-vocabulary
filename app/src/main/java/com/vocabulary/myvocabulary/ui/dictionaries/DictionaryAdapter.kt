package com.vocabulary.myvocabulary.ui.dictionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.room.dictionaryData.DictionaryEntry

class DictionaryAdapter(private val dictionaryList : List<DictionaryEntry>, private val itemClickListener: ItemClickListener) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
        val inflater =LayoutInflater.from(parent.context)
        return DictionaryViewHolder(inflater.inflate(R.layout.row_dictionary, parent, false))
    }

    override fun getItemCount(): Int {
        return dictionaryList.size
    }

    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
        if(holder.dictionaryName.text != null) {
            holder.dictionaryName.text = dictionaryList[position].dictionaryName
        }
    }

    inner class DictionaryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val dictionaryName : TextView = itemView.findViewById(R.id.dictionary_name)

        init{
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            itemClickListener.onItemClick(dictionaryList[adapterPosition])
        }
    }

    interface ItemClickListener {
        fun onItemClick(dictionaryEntry: DictionaryEntry)
    }
}