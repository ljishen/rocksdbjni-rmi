package org.rocksdb;

import java.util.List;

public class RocksDBImpl implements IRocksDB {
    private RocksDB rocksDb;

    @Override
    public RocksDB open(final DBOptions options, final String path,
                        final List<ColumnFamilyDescriptor> columnFamilyDescriptors,
                        final List<ColumnFamilyHandle> columnFamilyHandles) throws RocksDBException {
        rocksDb = RocksDB.open(options, path, columnFamilyDescriptors, columnFamilyHandles);
        return rocksDb;
    }

    @Override
    public void close() {
        rocksDb.close();
    }

    @Override
    public byte[] get(final ColumnFamilyHandle columnFamilyHandle,
                      final byte[] key) throws RocksDBException {
        return rocksDb.get(columnFamilyHandle, key);
    }

    @Override
    public RocksIterator newIterator(
            final ColumnFamilyHandle columnFamilyHandle) {
        return rocksDb.newIterator(columnFamilyHandle);
    }

    @Override
    public void put(final ColumnFamilyHandle columnFamilyHandle,
                    final byte[] key, final byte[] value) throws RocksDBException {
        rocksDb.put(columnFamilyHandle, key, value);
    }

    @Override
    public void delete(final ColumnFamilyHandle columnFamilyHandle,
                       final byte[] key) throws RocksDBException {
        rocksDb.delete(columnFamilyHandle, key);
    }

    @Override
    public ColumnFamilyHandle createColumnFamily(
            final ColumnFamilyDescriptor columnFamilyDescriptor) throws RocksDBException {
        return rocksDb.createColumnFamily(columnFamilyDescriptor);
    }
}
