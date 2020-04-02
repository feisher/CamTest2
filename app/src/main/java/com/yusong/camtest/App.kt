package com.yusong.camtest

import android.app.Application

/**
 * Created by feisher on 2020-04-01.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        KtxConfig.init(this)
    }
}