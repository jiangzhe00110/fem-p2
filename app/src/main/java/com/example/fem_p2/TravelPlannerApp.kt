package com.example.fem_p2

import android.app.Application
import com.google.firebase.FirebaseApp

class TravelPlannerApp : Application() {
    lateinit var appContainer: TravelPlannerContainer
        private set

    override fun onCreate() {
        super.onCreate()
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        appContainer = TravelPlannerContainer()
    }
}