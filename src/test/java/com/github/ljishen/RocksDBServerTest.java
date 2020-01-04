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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.rmi.RemoteException;

import static org.junit.Assert.assertEquals;

public class RocksDBServerTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void invalidPortNumber() throws RemoteException {
        thrown.expect(RemoteException.class);
        thrown.expectMessage("Invalid port number");
        RocksDBServer.getRegistryPort(0);
    }

    @Test
    public void invalidInetAddress() throws RemoteException {
        thrown.expect(RemoteException.class);
        thrown.expectMessage("Invalid hostname");
        RocksDBServer.getRegistryHost("255.255.255.0.1");
    }

    @Test
    public void validInetAddress() throws RemoteException {
        String ip = "172.17.0.2";
        assertEquals(ip, RocksDBServer.getRegistryHost(ip));
    }

    @Test
    public void invalidInternetDomainName() throws RemoteException {
        thrown.expect(RemoteException.class);
        thrown.expectMessage("Invalid hostname");
        RocksDBServer.getRegistryHost("_u.edu");
    }

    @Test
    public void validInternetDomainName() throws RemoteException {
        String hostname = "www.ucsc.edu";
        assertEquals(hostname, RocksDBServer.getRegistryHost(hostname));
    }
}
