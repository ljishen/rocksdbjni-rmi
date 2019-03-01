package org.rocksdb;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IRocksDB extends Remote {

    RocksDB open(final DBOptions options, final String path,
                 final List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                 final List<ColumnFamilyHandle> columnFamilyHandles)
            throws RemoteException, RocksDBException;


    void close() throws RemoteException;

    byte[] get(final ColumnFamilyHandle columnFamilyHandle,
               final byte[] key) throws RemoteException, RocksDBException;

    RocksIterator newIterator(
            final ColumnFamilyHandle columnFamilyHandle) throws RemoteException;

    void put(final ColumnFamilyHandle columnFamilyHandle,
             final byte[] key, final byte[] value)
            throws RemoteException, RocksDBException;

    void delete(final ColumnFamilyHandle columnFamilyHandle,
                final byte[] key) throws RemoteException, RocksDBException;

    ColumnFamilyHandle createColumnFamily(
            final ColumnFamilyDescriptor columnFamilyDescriptor)
            throws RemoteException, RocksDBException;
}
