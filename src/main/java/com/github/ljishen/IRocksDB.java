package com.github.ljishen;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

public interface IRocksDB extends Remote {

    void open(String rocksDbDir, String optionsFileName)
            throws RemoteException;

    void close() throws RemoteException;

    byte[] get(final String table, final String key)
            throws RemoteException;

    List<byte[]> batchGet(final String table,
                          final String startkey,
                          final int recordcount) throws RemoteException;

    void put(String table, String key, byte[] value) throws RemoteException;

    void delete(String table, String key) throws RemoteException;

    Set<String> getColumnFamilyNames() throws RemoteException;
}
