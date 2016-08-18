package com.timursoft.imtranslator.di

import android.app.Application
import com.timursoft.imtranslator.entity.Models
import dagger.Module
import dagger.Provides
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.rx.RxSupport
import io.requery.rx.SingleEntityStore
import io.requery.sql.EntityDataStore
import javax.inject.Singleton

@Module
class AndroidModule(private val application: Application) {

    @Provides
    @Singleton
    fun provideDataStore(): SingleEntityStore<Persistable> {
        val source = DatabaseSource(application, Models.DEFAULT, 1)
//        if (BuildConfig.DEBUG) {
//            // use this in development mode to drop and recreate the tables on every upgrade
//            source.setTableCreationMode(TableCreationMode.DROP_CREATE)
//        }
        return RxSupport.toReactiveStore(EntityDataStore<Persistable>(source.configuration))
    }

}
