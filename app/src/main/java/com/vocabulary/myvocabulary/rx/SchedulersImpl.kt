package com.vocabulary.myvocabulary.rx

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SchedulersImpl : RxSchedulers {
    override fun io() = Schedulers.io()

    override fun main() = AndroidSchedulers.mainThread()
}