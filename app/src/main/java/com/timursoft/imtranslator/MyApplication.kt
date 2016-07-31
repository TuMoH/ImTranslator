package com.timursoft.imtranslator

import android.app.Application
import com.timursoft.imtranslator.di.AndroidModule
import com.timursoft.imtranslator.di.AppComponent
import com.timursoft.imtranslator.di.DaggerAppComponent

class MyApplication : Application() {

    companion object {
        //platformStatic allow access it from java code
        @JvmStatic lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = DaggerAppComponent.builder().androidModule(AndroidModule(this)).build()
    }

}