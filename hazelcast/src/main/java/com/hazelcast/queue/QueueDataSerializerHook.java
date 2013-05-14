/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.queue;

import com.hazelcast.collection.operations.AddAllOperation;
import com.hazelcast.nio.serialization.*;
import com.hazelcast.util.ConstructorFunction;

/**
 * @mdogan 8/24/12
 */
public final class QueueDataSerializerHook implements DataSerializerHook {

    static final int F_ID = FactoryIdHelper.getFactoryId(FactoryIdHelper.QUEUE_DS_FACTORY, -11);

    static final int OFFER = 0;
    static final int POLL = 1;
    static final int PEEK = 2;

    static final int OFFER_BACKUP = 3;
    static final int POLL_BACKUP = 4;

    static final int ADD_ALL_BACKUP = 5;
    static final int ADD_ALL = 6;
    static final int CLEAR_BACKUP = 7;
    static final int CLEAR = 8;
    static final int COMPARE_AND_REMOVE_BACKUP = 9;
    static final int COMPARE_AND_REMOVE = 10;
    static final int CONTAINS = 11;
    static final int DRAIN_BACKUP = 12;
    static final int DRAIN = 13;
    static final int ITERATOR = 14;
    static final int QUEUE_EVENT = 15;
    static final int QUEUE_EVENT_FILTER = 16;
    static final int QUEUE_ITEM = 17;
    static final int QUEUE_REPLICATION = 18;
    static final int REMOVE_BACKUP = 19;
    static final int REMOVE = 20;
    static final int COLLECTION_CONTAINER = 21;
    static final int SIZE = 22;

    public int getFactoryId() {
        return F_ID;
    }

    public DataSerializableFactory createFactory() {

        ConstructorFunction<Integer, IdentifiedDataSerializable> constructors[] = new ConstructorFunction[23];
        
        constructors[OFFER] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new OfferOperation();
            }
        };
        
        constructors[OFFER_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new OfferBackupOperation();
            }
        };
        constructors[POLL] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new PollOperation();
            }
        };
        constructors[POLL_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new PollBackupOperation();
            }
        };
        constructors[PEEK] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new PeekOperation();
            }
        };
        constructors[ADD_ALL_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new AddAllBackupOperation();
            }
        };
        constructors[ADD_ALL] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new AddAllOperation();
            }
        };
        constructors[CLEAR_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new ClearBackupOperation();
            }
        };
        constructors[CLEAR] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new ClearOperation();
            }
        };
        constructors[COMPARE_AND_REMOVE_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new CompareAndRemoveBackupOperation();
            }
        };
        constructors[COMPARE_AND_REMOVE] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new CompareAndRemoveOperation();
            }
        };
        constructors[CONTAINS] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new ContainsOperation();
            }
        };
        constructors[DRAIN_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new DrainBackupOperation();
            }
        };
        constructors[DRAIN] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new DrainOperation();
            }
        };
        constructors[ITERATOR] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new IteratorOperation();
            }
        };
        constructors[QUEUE_EVENT] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new QueueEvent();
            }
        };
        constructors[QUEUE_EVENT_FILTER] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new QueueEventFilter();
            }
        };
        constructors[QUEUE_ITEM] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new PeekOperation();
            }
        };
        constructors[QUEUE_REPLICATION] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new QueueReplicationOperation();
            }
        };
        constructors[REMOVE_BACKUP] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new RemoveBackupOperation();
            }
        };
        constructors[REMOVE] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new RemoveOperation();
            }
        };
        constructors[COLLECTION_CONTAINER] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new SerializableCollectionContainer();
            }
        };
        constructors[SIZE] = new ConstructorFunction<Integer, IdentifiedDataSerializable>() {
            public IdentifiedDataSerializable createNew(Integer arg) {
                return new SizeOperation();
            }
        };


        return new ArrayDataSerializableFactory(constructors);
    }
}
