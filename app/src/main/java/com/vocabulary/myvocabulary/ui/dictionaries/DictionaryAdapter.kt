package com.vocabulary.myvocabulary.ui.dictionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.databinding.RowDictionaryBinding

class DictionaryAdapter(
    private var dictionaryList: List<Dictionary>,
    private val itemClickListener: ItemClickListener,
    private val allowItemEditing: Boolean
) : RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
        val binding =
            RowDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DictionaryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dictionaryList.size
    }

    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
        holder.bind(dictionaryList[position])

    }

    inner class DictionaryViewHolder(private val binding: RowDictionaryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(dictionary: Dictionary) {
            binding.dictionaryName.text = dictionary.dictionaryName
            binding.dictionaryOptions.display(allowItemEditing)
        }

        init {
            binding.dictionaryName.setOnClickListener {
                itemClickListener.onItemClick(dictionaryList[adapterPosition])
            }

            binding.dictionaryOptions.setOnClickListener {
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