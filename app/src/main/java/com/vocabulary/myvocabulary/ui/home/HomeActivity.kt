package com.vocabulary.myvocabulary.ui.home

import android.annotation.SuppressLint
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.vocabulary.myvocabulary.R
import kotlinx.android.synthetic.main.activity_home.*
import android.support.design.widget.TabLayout



class HomeActivity : AppCompatActivity() {

    @SuppressLint("PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tabView.addTab(tabView.newTab().setText("Dictionaries"))
        tabView.addTab(tabView.newTab().setText("Tests"))
//        tabView.tabGravity(TabLayout.GRAVITY_FILL)

        val sectionAdapter = PagerAdapter(supportFragmentManager)
        homeViewPager.adapter = sectionAdapter
        tabView.setupWithViewPager(homeViewPager)
        tabView.setTabTextColors(R.color.primary_text_default_material_dark, Color.WHITE)

        tabView.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                homeViewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })
    }
}
