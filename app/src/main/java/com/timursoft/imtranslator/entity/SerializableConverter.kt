package com.timursoft.imtranslator.entity

import io.requery.Converter
import java.io.*

abstract class SerializableConverter<T : Serializable>(private val type: Class<T>) : Converter<T, ByteArray> {

    override fun getMappedType(): Class<T> {
        return type
    }

    override fun getPersistedType(): Class<ByteArray> {
        return ByteArray::class.java
    }

    override fun getPersistedSize(): Int? {
        return null
    }

    override fun convertToPersisted(value: T): ByteArray {
        return ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use { out ->
                out.writeObject(value)
            }
            bos
        }.toByteArray()
    }

    override fun convertToMapped(type: Class<out T>, value: ByteArray?): T? {
        return ByteArrayInputStream(value).use { bis ->
            ObjectInputStream(bis).use { ois ->
                type.cast(ois.readObject())
            }
        }
    }
}