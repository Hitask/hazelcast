/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.nio.serialization;

import com.hazelcast.nio.ClassLoaderUtil;
import com.hazelcast.nio.DataSerializable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @mdogan 5/16/12
 */
public class SerializerManager {

    private static int ID;

    private static synchronized int nextId() {
        return ID++;
    }

    private final int id = nextId();

    private final AtomicReference<TypeSerializer> global = new AtomicReference<TypeSerializer>();

    private final ConcurrentMap<Class, TypeSerializer> typeMap = new ConcurrentHashMap<Class, TypeSerializer>();

    private final ConcurrentMap<Integer, TypeSerializer> idMap = new ConcurrentHashMap<Integer, TypeSerializer>();

    {
        safeRegister(DataSerializable.class, new DataSerializer());
    }

    public void register(final TypeSerializer serializer, final Class type) {
        if (type == null) {
            throw new IllegalArgumentException("Class type information is required!");
        }
        if (serializer.getTypeId() < SerializationConstants.EXTERNAL_TYPE_ID_MIN) {
            throw new IllegalArgumentException("Type id must be greater than " + SerializationConstants.EXTERNAL_TYPE_ID_MIN
                                               + "! Current: " + serializer.getTypeId());
        }
        safeRegister(type, serializer);
    }

    public void registerGlobal(final TypeSerializer serializer) {
        if (!global.compareAndSet(null, serializer)) {
            throw new IllegalStateException("Global serializer is already registered!");
        }
    }

    public void deregister(final Class type) {
        final TypeSerializer factory = typeMap.remove(type);
        if (factory != null) {
            idMap.remove(factory.getTypeId());
        }
    }

    public void deregisterGlobal() {
        global.set(null);
    }

    public TypeSerializer serializerFor(final Class type) {
        TypeSerializer serializer = typeMap.get(type);
        if (serializer == null) {
            if (DataSerializable.class.isAssignableFrom(type)
                    && ClassLoaderUtil.isInternalType(type)) {
                serializer = registerFromSuperType(type, DataSerializable.class);
            } else {
                // look for super classes
                Class typeSuperclass = type.getSuperclass();
                List<Class> interfaces = new LinkedList<Class>();
                Collections.addAll(interfaces, type.getInterfaces());
                while (typeSuperclass != null) {
                    if ((serializer = registerFromSuperType(type, typeSuperclass)) != null) {
                        break;
                    }
                    Collections.addAll(interfaces, typeSuperclass.getInterfaces());
                    typeSuperclass = typeSuperclass.getSuperclass();
                }
                if (serializer == null) {
                    // look for interfaces
                    for (Class typeInterface : interfaces) {
                        if ((serializer = registerFromSuperType(type, typeInterface)) != null) {
                            break;
                        }
                    }
                }
                if (serializer == null && (serializer = global.get()) != null) {
                    safeRegister(type, serializer);
                }
                if (serializer == null && (serializer = DefaultSerializers.serializerFor(type)) != null) {
                    safeRegister(type, serializer);
                }
            }
        }
        return serializer;
    }

    private TypeSerializer registerFromSuperType(final Class type, final Class superType) {
        final TypeSerializer serializer = typeMap.get(superType);
        if (serializer != null) {
            safeRegister(type, serializer);
        }
        return serializer;
    }

    private void safeRegister(final Class type, final TypeSerializer serializer) {
        if (DataSerializable.class.isAssignableFrom(type)
            && ClassLoaderUtil.isInternalType(type)
            && serializer.getClass() != DataSerializer.class) {
            throw new IllegalArgumentException("Internal DataSerializable[" + type + "] " +
                    "serializer cannot be overridden!");
        }
        System.err.println("Register Serializer: " + type + " -> " + serializer.getClass());
        TypeSerializer f = typeMap.putIfAbsent(type, serializer);
        if (f != null && f.getClass() != serializer.getClass()) {
            throw new IllegalStateException("Serializer has been already registered for type: " + type);
        }
        f = idMap.putIfAbsent(serializer.getTypeId(), serializer);
        if (f != null && f.getClass() != serializer.getClass()) {
            throw new IllegalStateException("Serializer has been already registered for type-id: " + serializer.getTypeId());
        }
    }

    public TypeSerializer serializerFor(final int typeId) {
        TypeSerializer serializer = idMap.get(typeId);
        if (serializer == null) {
            serializer = DefaultSerializers.serializerFor(typeId);
        }
        return serializer;
    }

    public int getId() {
        return id;
    }

    public void destroy() {
        for (TypeSerializer serializer : typeMap.values()) {
            serializer.destroy();
        }
        typeMap.clear();
        idMap.clear();
        global.set(null);
    }
}
