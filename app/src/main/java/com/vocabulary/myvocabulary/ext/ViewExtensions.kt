package com.vocabulary.myvocabulary.ext

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet

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

fun View.fadeoutAnimation(duration: Long = 500) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.startOffset = 1000
    fadeOut.duration = duration

    val fadeAnimation = AnimationSet(false)
    fadeAnimation.addAnimation(fadeOut)
    this.animation = fadeAnimation
}