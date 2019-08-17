package com.vocabulary.myvocabulary.ext

import android.view.View

fun View.show(visible: Boolean) {
    visibility = if (visible) {
        View.VISIBLE
    } else {
        View.INVISIBLE
    }
}

fun View.display(displayed: Boolean) {
    visibility = if (displayed) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.displayWithAnimation(displayed: Boolean) {
    this.animate().translationY(this.height.toFloat())
}