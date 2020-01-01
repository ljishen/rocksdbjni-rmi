/*
 * Copyright (c) 2018 YCSB contributors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.github.ljishen;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.RocksDB;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class RocksDBImplTest {

    private static final String MOCK_TABLE = "ycsb";
    private static final String MOCK_KEY = "I'm a key";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private IRocksDB instance;

    @Before
    public void setup() throws RemoteException {
        instance = new RocksDBImpl();
        instance.open(tmpFolder.getRoot().getAbsolutePath(),
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("OPTIONS")).getFile());
    }

    @After
    public void tearDown() throws RemoteException {
        instance.close();
    }

    @Test
    public void recoverFromExternalOptionsFile() throws RemoteException {
        // The column families should be able to be recovered by reading
        // from the external options file.
        assertEquals(instance.getColumnFamilyNames(),
                new HashSet<>(
                        Arrays.asList(new String(RocksDB.DEFAULT_COLUMN_FAMILY, UTF_8),
                                "myusertable")));
    }

    @Test
    public void addNewColumnFamilyByGet() throws RemoteException {
        instance.get(MOCK_TABLE, MOCK_KEY);
        assertEquals(instance.getColumnFamilyNames(),
                new HashSet<>(
                        Arrays.asList(new String(RocksDB.DEFAULT_COLUMN_FAMILY, UTF_8),
                                MOCK_TABLE, "myusertable")));
    }

    @Test
    public void inertAndGet() throws RemoteException {
        assertNull(instance.get(MOCK_TABLE, MOCK_KEY));

        byte[] value = valueOfKey(MOCK_KEY);

        instance.put(MOCK_TABLE, MOCK_KEY, value);
        assertArrayEquals(value, instance.get(MOCK_TABLE, MOCK_KEY));
    }

    @Test
    public void insertAndDelete() throws RemoteException {
        byte[] value = valueOfKey(MOCK_KEY);

        instance.put(MOCK_TABLE, MOCK_KEY, value);
        instance.delete(MOCK_TABLE, MOCK_KEY);
        assertNull(instance.get(MOCK_TABLE, MOCK_KEY));
    }

    @Test
    public void bulkGet() throws RemoteException {
        int numKeys = 20;
        for (int i = 0; i < numKeys; i++) {
            String key = keyAt(i);
            instance.put(MOCK_TABLE, key, valueOfKey(key));
        }

        int startKeyIdx = 9, recordCount = 10;
        List<byte[]> values = instance.bulkGet(MOCK_TABLE, keyAt(startKeyIdx), recordCount);
        for (int i = startKeyIdx; i < recordCount; i++) {
            assertArrayEquals(values.get(i - startKeyIdx), valueOfKey(keyAt(i)));
        }
    }

    private String keyAt(int index) {
        return "key" + index;
    }

    private byte[] valueOfKey(String key) {
        return ("value" + key).getBytes();
    }
}
