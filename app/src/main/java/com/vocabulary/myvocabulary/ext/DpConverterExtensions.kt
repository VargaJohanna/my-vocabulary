package com.vocabulary.myvocabulary.ext

import android.content.Context
import android.util.TypedValue

fun Float.convertDpToPx(context: Context): Int {
    return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            context.resources.displayMetrics
    ).toInt()
}