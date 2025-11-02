package com.vocabulary.myvocabulary.ui.words

import android.view.View
import com.vocabulary.myvocabulary.R
import com.xwray.groupie.Item
import com.vocabulary.myvocabulary.databinding.RowWordBinding
import com.xwray.groupie.viewbinding.BindableItem

class WordItem(
        val wordData: Word,
        private val onClick: (selectedItem: WordItem) -> Unit
) : BindableItem<RowWordBinding>(wordData.wordId) {
    override fun initializeViewBinding(view: View): RowWordBinding {
        return RowWordBinding.bind(view)
    }

    override fun bind(binding: RowWordBinding, position: Int) {
        binding.word.text = wordData.word
        binding.translation.text = wordData.translation
        binding.root.setOnClickListener { onClick(this@WordItem) }

    }

    override fun getLayout() = R.layout.row_word

    //TODO: Can hasSameContentAs() replace isSameAs()
//    override fun isSameAs(other: com.xwray.groupie.Item<*>?) = wordData.wordId == (other as? WordItem)?.wordData?.wordId

    override fun hasSameContentAs(other: Item<*>): Boolean {
        return other is WordItem && this.wordData.wordId == other.wordData.wordId
    }

    override fun equals(other: Any?) = wordData.wordId == (other as? WordItem)?.wordData?.wordId
}