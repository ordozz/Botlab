package com.example.tnotes

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TNotesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialization code can go here if needed
    }
}
