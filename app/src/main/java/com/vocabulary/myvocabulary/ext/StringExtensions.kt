package com.vocabulary.myvocabulary.ext

fun String.stringToInt(): Int =
    if(this.isEmpty()) {
        0
    } else {
        this.toInt()
    }
