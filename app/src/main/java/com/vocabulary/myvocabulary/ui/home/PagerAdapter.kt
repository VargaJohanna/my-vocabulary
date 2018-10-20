package com.vocabulary.myvocabulary.ui.home

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter

class PagerAdapter(fm: android.support.v4.app.FragmentManager) : FragmentPagerAdapter (fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> DictionariesFragment()
            1 -> TestsFragment()
            else -> DictionariesFragment()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Dictionaries"
            1 -> "Tests"
            else -> "Select a tab"
        }
    }

}