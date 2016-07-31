package com.timursoft.imtranslator.di

import com.timursoft.imtranslator.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(AndroidModule::class))
interface AppComponent {

    fun inject(mainActivity: MainActivity)

}