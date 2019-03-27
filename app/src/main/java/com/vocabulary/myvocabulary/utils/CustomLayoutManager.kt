//package com.vocabulary.myvocabulary.utils
//
//import android.content.Context
//import androidx.recyclerview.widget.LinearLayoutManager
//
//class CustomLayoutManager(context: Context) : LinearLayoutManager(context) {
//
//    private var isScrollEnabled = true
//
//    fun setScrollEnabled(flag: Boolean) {
//        isScrollEnabled = flag
//    }
//
//    override fun canScrollVertically(): Boolean {
//        return isScrollEnabled && super.canScrollHorizontally()
//    }
//
//}