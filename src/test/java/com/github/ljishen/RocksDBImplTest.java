/*
 * MIT License
 *
 * Copyright (c) 2020 Jianshen Liu
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.ljishen;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.rocksdb.RocksDB;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

public class RocksDBImplTest {

    private static final String MOCK_TABLE = "ycsb";
    private static final String MOCK_KEY = "I'm a key";

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private IRocksDB instance;

    @Before
    public void setup() throws IOException {
        instance = new RocksDBImpl();

        String activeProfile = System.getProperty("activeProfile", "rocksdb");
        String rocksdbVersion = System.getProperty("rocksdbVersion");
        String optionsFileName = "OPTIONS." + activeProfile + "-v" + rocksdbVersion;
        URL optionsFileURL = getClass().getClassLoader().getResource(optionsFileName);
        if (optionsFileURL == null) {
            throw new IOException("Cannot find OPTIONS file: " + optionsFileName);
        }
        instance.open(tmpFolder.getRoot().getAbsolutePath(), optionsFileURL.getFile());
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
