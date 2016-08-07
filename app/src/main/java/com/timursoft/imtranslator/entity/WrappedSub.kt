package com.timursoft.imtranslator.entity

import android.os.Parcelable
import com.timursoft.suber.Sub
import io.requery.*

@Entity
interface WrappedSub : Persistable, Parcelable {

    @get:Key
    @get:Generated
    var id: Int

    @get:Convert(SubConverter::class)
    var sub: Sub

    var time: String
    var originalContent: String
    var modified: Boolean

    @get:ManyToOne
    var subFile: SubFile

}