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
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class RocksDBImplTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private IRocksDB instance;

    @Before
    public void setup() {
        instance = new RocksDBImpl();
    }

    @After
    public void tearDown() throws RemoteException {
        instance.close();
    }

    @Test
    public void recoverFromExternalOptionsFile() throws RemoteException {
        instance.open(tmpFolder.getRoot().getAbsolutePath(),
                Objects.requireNonNull(
                        getClass().getClassLoader().getResource("OPTIONS")).getFile());

        // The column families should be able to be recovered by reading
        // from the external options file.
        assertEquals(instance.getColumnFamilyNames(),
                new HashSet<>(
                        Arrays.asList(new String(RocksDB.DEFAULT_COLUMN_FAMILY, UTF_8),
                                "myusertable")));
    }
}
