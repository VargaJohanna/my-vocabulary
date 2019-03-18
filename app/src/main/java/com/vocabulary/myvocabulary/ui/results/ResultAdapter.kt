package com.vocabulary.myvocabulary.ui.results

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ui.words.Word
import com.vocabulary.myvocabulary.ui.words.WordDiffUtilCallBack

class ResultAdapter(
        private var resultList: List<Word>
) : RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ResultViewHolder(inflater.inflate(R.layout.row_result, parent, false))
    }

    override fun getItemCount(): Int = resultList.size

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(resultList[position])
    }

    inner class ResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(word: Word) {

        }
    }

    fun updateList(newList: List<Word>) {
        val diffResult = DiffUtil.calculateDiff(WordDiffUtilCallBack(resultList, newList))
        this.resultList = newList
        diffResult.dispatchUpdatesTo(this)
    }

}