package com.example.flappy

import android.app.Application
import com.example.flappy.mvvm.appwriteModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FlappyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FlappyApp)
            modules(listOf(appwriteModule))
        }
    }
}
