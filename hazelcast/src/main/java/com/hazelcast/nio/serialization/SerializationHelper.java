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

import com.hazelcast.core.ManagedContext;
import com.hazelcast.core.PartitionAware;
import com.hazelcast.impl.ThreadContext;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.Logger;
import com.hazelcast.nio.Data;
import com.hazelcast.nio.FastDataInputStream;
import com.hazelcast.nio.FastDataOutputStream;
import com.hazelcast.nio.HazelcastSerializationException;

import java.io.NotSerializableException;
import java.util.logging.Level;

public final class SerializationHelper {

    private static final int OUTPUT_STREAM_BUFFER_SIZE = 100 << 10;

    private static final ILogger logger = Logger.getLogger(SerializationHelper.class.getName());

//    private final Map<SKey, TypeSerializer> serializerClassMap = new HashMap<SKey, TypeSerializer>();
//    private final Map<SKey, TypeSerializer> serializerTypeMap = new HashMap<SKey, TypeSerializer>();

//    private final Map<Class, TypeSerializer> serializerClassMap = new HashMap<Class, TypeSerializer>();
//    private final Map<Integer, TypeSerializer> serializerTypeMap = new HashMap<Integer, TypeSerializer>();

    private final FastDataOutputStream dataOut;
    private final FastDataInputStream dataIn;
    private final ThreadContext context;
    private final SerializerManager defaultManager ;

    public SerializationHelper(SerializerManager manager) {
        dataIn = new FastDataInputStream(new byte[0]);
        dataOut = new FastDataOutputStream(OUTPUT_STREAM_BUFFER_SIZE);
        context = null;
        defaultManager = manager;
    }

    public SerializationHelper(ThreadContext ctx) {
        dataIn = new FastDataInputStream(new byte[0]);
        dataOut = new FastDataOutputStream(OUTPUT_STREAM_BUFFER_SIZE);
        context = ctx;
        defaultManager = null;
    }

    private void writeObject(final FastDataOutputStream out, final Object object) {
        if (object == null) {
            return;
        }
        try {
//            TypeSerializer serializer = serializerFor(object);
            final TypeSerializer serializer = getTypeSerializerManager().serializerFor(object.getClass());
            if (serializer == null) {
                throw new NotSerializableException("There is no suitable serializer for " + object.getClass());
            }
            out.writeInt(serializer.getTypeId());
            serializer.write(out, object);
            out.flush();
        } catch (Throwable e) {
            throw new HazelcastSerializationException(e);
        }
    }

    private Object readObject(final FastDataInputStream in) {
        int typeId = -1;
        try {
            typeId = in.readInt();
//            TypeSerializer serializer = serializerFor(typeId);
            final TypeSerializer serializer = getTypeSerializerManager().serializerFor(typeId);
            if (serializer == null) {
                throw new IllegalArgumentException("There is no suitable de-serializer for type " + typeId);
            }
            return serializer.read(in);
        } catch (Throwable e) {
            if (e instanceof HazelcastSerializationException) {
                throw (HazelcastSerializationException) e;
            }
            throw new HazelcastSerializationException("Problem while serializing type " + typeId, e);
        }
    }

//    private TypeSerializer serializerFor(final Object obj) {
//        final Class clazz = obj.getClass();
//        final SerializerManager manager = getTypeSerializerManager();
////        final SKey key = new SKey(manager.getId(), clazz);
////        TypeSerializer serializer = serializerClassMap.get(key);
//        TypeSerializer serializer = serializerClassMap.get(clazz);
//        if (serializer == null) {
//            serializer = manager.serializerFor(clazz);
//            if (serializer != null) {
////                System.err.println(key);
////                serializerClassMap.put(key, serializer);
////                serializerTypeMap.put(new SKey(manager.getId(), serializer.getTypeId()), serializer);
//                serializerClassMap.put(clazz, serializer);
//                serializerTypeMap.put(serializer.getTypeId(), serializer);
//            }
//        }
//        return serializer;
//    }
//
//    private TypeSerializer serializerFor(final int typeId) {
//        final SerializerManager manager = getTypeSerializerManager();
////        final SKey key = new SKey(manager.getId(), typeId);
////        TypeSerializer serializer = serializerTypeMap.get(key);
//        TypeSerializer serializer = serializerTypeMap.get(typeId);
//        if (serializer == null) {
//            serializer = manager.serializerFor(typeId);
//            if (serializer != null) {
////                System.err.println(key);
////                serializerTypeMap.put(key, serializer);
//                serializerTypeMap.put(typeId, serializer);
//            }
//        }
//        return serializer;
//    }

    private SerializerManager getTypeSerializerManager() {
        final SerializerManager serializerManager = context != null ? context.getCurrentSerializerManager()
                                                                    : defaultManager;
        if (serializerManager == null) {
            throw new HazelcastSerializationException("SerializerManager could not be found!");
        }
        return serializerManager;
    }

    public byte[] toByteArray(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            dataOut.reset();
            writeObject(dataOut, obj);
            final byte[] result = dataOut.toByteArray();
            if (dataOut.size() > OUTPUT_STREAM_BUFFER_SIZE) {
                dataOut.set(new byte[OUTPUT_STREAM_BUFFER_SIZE]);
            }
            return result;
        } catch (Throwable e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new HazelcastSerializationException(e);
        }
    }

    public Object toObject(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return null;
        }
        dataIn.set(byteArray, byteArray.length);
        final Object obj = readObject(dataIn);
        dataIn.set(null, 0);
        return obj;
    }

    public Data writeObject(final Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Data) {
            return (Data) obj;
        }
        final byte[] bytes = toByteArray(obj);
        if (bytes == null) {
            return null;
        } else {
            final Data data = new Data(bytes);
            if (obj instanceof PartitionAware) {
                final Data partitionKey = writeObject(((PartitionAware) obj).getPartitionKey());
                final int partitionHash = (partitionKey == null) ? -1 : partitionKey.getPartitionHash();
                data.setPartitionHash(partitionHash);
            }
            return data;
        }
    }

    public Object readObject(final Data data) {
        if ((data == null) || (data.buffer == null) || (data.buffer.length == 0)) {
            return null;
        }
        final Object obj = toObject(data.buffer);
        final ManagedContext managedContext = context != null
                                              ? context.getCurrentManagedContext() : null;
        if (managedContext != null) {
            managedContext.initialize(obj);
        }
        return obj;
    }

    public void destroy() {
        dataOut.set(null);
        dataIn.set(null, 0);
//        serializerClassMap.clear();
//        serializerTypeMap.clear();
    }

    private final class SKey {
        final Object main;
        final Object sub;

        SKey(final Object main, final Object sub) {
            this.main = main;
            this.sub = sub;
        }

        @Override
        public boolean equals(final Object o) {
            // no need to check class
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;

            final SKey that = (SKey) o;
            if (!main.equals(that.main)) return false;
            if (!sub.equals(that.sub)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return 37 * main.hashCode() + sub.hashCode();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("SKey");
            sb.append("{main=").append(main);
            sb.append(", sub=").append(sub);
            sb.append('}');
            return sb.toString();
        }
    }
}
