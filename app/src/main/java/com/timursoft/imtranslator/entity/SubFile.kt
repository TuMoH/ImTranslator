package com.timursoft.imtranslator.entity

import android.os.Parcelable
import io.requery.*

@Entity
interface SubFile : Persistable, Parcelable {

    @get:Key
    @get:Generated
    var id: Int

    var name: String
    var filePath: String
    var videoPath: String?
    var imgPath: String?
    var uptime: Long

    var percent: Int
    var lastPosition: Int

    @get:OneToMany(mappedBy = "subFile", cascade = arrayOf(CascadeAction.DELETE, CascadeAction.SAVE))
    val subs: MutableList<WrappedSub>

}