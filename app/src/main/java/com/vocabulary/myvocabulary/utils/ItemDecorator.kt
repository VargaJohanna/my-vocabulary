package com.vocabulary.myvocabulary.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorator(private val intSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        var previousHeight: Int
        val currentItemPosition = parent.indexOfChild(view)
        val previousItem = parent.getChildAt(currentItemPosition - 1)

        if (currentItemPosition != 0) {
            previousHeight = previousItem.height

        } else {
            previousHeight = 120 + intSpace
        }
        val actual = -(previousHeight - intSpace)
        outRect.top = actual
    }

}