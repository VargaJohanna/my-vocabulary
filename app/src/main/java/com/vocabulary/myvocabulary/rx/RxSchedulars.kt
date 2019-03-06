package com.vocabulary.myvocabulary.rx

import io.reactivex.Scheduler

interface RxSchedulars {
    fun io(): Scheduler
    fun main(): Scheduler
}