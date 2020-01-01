package com.github.ljishen;

import org.junit.Before;
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
