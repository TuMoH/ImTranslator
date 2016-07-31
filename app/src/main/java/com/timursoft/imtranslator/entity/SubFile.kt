package com.timursoft.imtranslator.entity

import android.databinding.Bindable
import android.databinding.Observable
import android.os.Parcelable
import io.requery.*

@Entity
interface SubFile : Persistable, Parcelable, Observable {

    @get:Key
    @get:Generated
    var id: Int

    @get:Bindable
    var name: String
    var filePath: String
    var videoPath: String
    var imgPath: String
    var uptime: Long

    @get:OneToMany(mappedBy = "subFile", cascade = arrayOf(CascadeAction.DELETE, CascadeAction.SAVE))
    val subs: MutableList<WrappedSub>

}