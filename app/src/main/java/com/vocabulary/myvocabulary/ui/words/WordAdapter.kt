package com.vocabulary.myvocabulary.ui.words

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.row_word.view.*

class WordAdapter(private var wordList: List<Word>, private val itemClickListener: WordItemClickListener) : RecyclerView.Adapter<WordAdapter.WordViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordAdapter.WordViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return WordViewHolder(inflater.inflate(R.layout.row_word, parent, false))
    }

    override fun getItemCount() = wordList.size

    override fun onBindViewHolder(holder: WordAdapter.WordViewHolder, position: Int) {
        holder.bind(wordList[position])
    }

    inner class WordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        init {
            itemView.word.setOnClickListener{
                itemClickListener.onItemClick(wordList[adapterPosition])
            }
            itemView.translation.setOnClickListener{
                itemClickListener.onItemClick(wordList[adapterPosition])
            }

            itemView.word_options.setOnClickListener {
                itemClickListener.onOptionsClick(wordList[adapterPosition], it)
            }
        }

        fun bind(word: Word) {
            itemView.word.text = word.word
            itemView.translation.text = word.translation
        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(wordList, newList))
        this.wordList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    interface WordItemClickListener {
        fun onItemClick(word: Word)
        fun onOptionsClick(word: Word, view: View)
    }
}
