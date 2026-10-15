package com.colorbrowser

import android.app.Application

class ColorBrowserApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ColorBrowserApp
    }
}
