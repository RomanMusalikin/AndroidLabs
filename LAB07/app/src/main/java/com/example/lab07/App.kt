package com.example.lab07

import android.app.Application
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.search.SearchFactory
import com.yandex.mapkit.transport.TransportFactory

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MapKitFactory.setApiKey("2326f574-5c81-4819-86a6-e429b882b15c")
        MapKitFactory.initialize(this)
    }
}