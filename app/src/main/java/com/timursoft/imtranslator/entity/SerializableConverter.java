package com.timursoft.imtranslator.entity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import io.requery.Converter;

public abstract class SerializableConverter<T extends Serializable> implements Converter<T, byte[]> {

    private final Class<T> type;

    public SerializableConverter(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        this.type = type;
    }

    @Override
    public Class<T> getMappedType() {
        return type;
    }

    @Override
    public Class<byte[]> getPersistedType() {
        return byte[].class;
    }

    @Override
    public Integer getPersistedSize() {
        return null;
    }

    @Override
    public byte[] convertToPersisted(T value) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(value);
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return new byte[0];
    }

    @Override
    public T convertToMapped(Class<? extends T> type, byte[] value) {
        ByteArrayInputStream bis = new ByteArrayInputStream(value);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return type.cast(in.readObject());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return null;
    }
}