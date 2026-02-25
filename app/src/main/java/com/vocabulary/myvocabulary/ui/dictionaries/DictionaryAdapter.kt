package com.vocabulary.myvocabulary.ui.dictionaries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vocabulary.myvocabulary.R
import com.vocabulary.myvocabulary.ext.display
import com.vocabulary.myvocabulary.databinding.RowDictionaryBinding
import java.util.Calendar

class DictionaryAdapter(
    private var dictionaryList: List<Dictionary>,
    private val itemClickListener: ItemClickListener,
    private val allowItemEditing: Boolean
) {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DictionaryViewHolder {
//        val binding =
//            RowDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return DictionaryViewHolder(binding)
//    }

     fun getItemCount(): Int {
        return dictionaryList.size
    }

//    override fun onBindViewHolder(holder: DictionaryViewHolder, position: Int) {
//        holder.bind(dictionaryList[position])
//
//    }

//    inner class DictionaryViewHolder(private val binding: RowDictionaryBinding) :
//        RecyclerView.ViewHolder(binding.root) {
//        fun bind(dictionary: Dictionary) {
//            binding.dictionaryName.text = dictionary.dictionaryName
//            binding.dictionaryOptions.display(allowItemEditing)
//        }
//
//        init {
//            binding.dictionaryName.setOnClickListener {
//                itemClickListener.onItemClick(dictionaryList[adapterPosition])
//            }
//
//            binding.dictionaryOptions.setOnClickListener {
//                itemClickListener.onOptionsClick(dictionaryList[adapterPosition], it)
//            }
//        }
//    }

    @Composable
    fun DictionaryItem(dictionary: Dictionary, modifier: Modifier = Modifier) {
        Row(modifier.fillMaxWidth()) {
            Text(text = dictionary.dictionaryName)
        }
    }

    @Composable
    fun DictionaryList() {
        LazyColumn(Modifier.fillMaxSize()) {
            items(dictionaryList) { item ->
                DictionaryItem(item)
            }
        }
    }

    fun updateList(newList: List<Dictionary>) {
        val diffResult = DiffUtil.calculateDiff(DictionaryDiffUtilCallBack(dictionaryList, newList))
        this.dictionaryList = newList
//        diffResult.dispatchUpdatesTo(this)
    }

    interface ItemClickListener {
        fun onItemClick(dictionary: Dictionary)
        fun onOptionsClick(dictionary: Dictionary, view: View)
    }
}


@Composable
fun DictionaryItem(dictionary: Dictionary, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth()) {
        Text(text = dictionary.dictionaryName)
    }
}