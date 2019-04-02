package com.vocabulary.myvocabulary.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorator(private val intSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        var previousHeight: Int
        val currentItemPosition = parent.indexOfChild(view)
        val previousItem = parent.getChildAt(currentItemPosition - 1)

        previousHeight = (if (currentItemPosition != 0) previousItem.height else 120 + intSpace)

        outRect.top = if(currentItemPosition != 0) -(previousHeight - intSpace) else 0
    }

}