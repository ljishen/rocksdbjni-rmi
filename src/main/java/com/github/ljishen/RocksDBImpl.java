package com.github.ljishen;

import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RocksDBImpl implements IRocksDB {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDBImpl.class);

    private DBOptions dbOptions;

    private RocksDB rocksDb;

    private ColumnFamilyOptions cfOptions;

    private Map<String, ColumnFamilyHandle> columnFamilies = new ConcurrentHashMap<>();


    private DBOptions getDefaultDBOptions() {
        final int rocksThreads = Runtime.getRuntime().availableProcessors() * 2;

        return new DBOptions()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true)
                .setIncreaseParallelism(rocksThreads)
                .setMaxBackgroundCompactions(rocksThreads)
                .setInfoLogLevel(InfoLogLevel.INFO_LEVEL);
    }

    @Override
    public void open(String rocksDbDir, String optionsFile) throws RemoteException {
        Path rocksDbDirPath = Paths.get(rocksDbDir).toAbsolutePath();
        LOGGER.info("RocksDB data dir: " + rocksDbDirPath);

        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();

        String optionsFilePath = optionsFile;
        if (optionsFilePath == null) {
            try {
                optionsFilePath = OptionsUtil.getLatestOptionsFileName(
                        rocksDbDirPath.toString(), Env.getDefault());
            } catch (RocksDBException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RemoteException(e.getMessage(), e);
            }
        }

        cfOptions = new ColumnFamilyOptions().optimizeLevelStyleCompaction();

        List<ColumnFamilyDescriptor> cfDescs;
        if (optionsFilePath.isEmpty()) {
            dbOptions = getDefaultDBOptions();
            cfDescs = Collections.singletonList(new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOptions));
        } else {
            dbOptions = new DBOptions();
            cfDescs = new ArrayList<>();

            try {
                // We don't wnat to hide incompatible options
                OptionsUtil.loadOptionsFromFile(
                        rocksDbDirPath.resolve(optionsFilePath).toString(),
                        Env.getDefault(),
                        dbOptions,
                        cfDescs);
            } catch (RocksDBException e) {
                LOGGER.error(e.getMessage(), e);
                throw new RemoteException(e.getMessage(), e);
            }

            LOGGER.info(
                    "Load column families: "
                            + cfDescs
                            .stream()
                            .map(cf -> new String(cf.getName(), UTF_8))
                            .collect(Collectors.toList())
                            .toString()
                            + " from options file: "
                            + optionsFilePath);
        }

        final List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
        try {
            rocksDb = RocksDB.open(dbOptions, rocksDbDirPath.toString(), cfDescs, cfHandles);
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }

        for (int i = 0; i < cfDescs.size(); i++) {
            columnFamilies.put(new String(cfDescs.get(i).getName(), UTF_8), cfHandles.get(i));
        }
    }

    @Override
    public void close() throws RemoteException {
        try {
            for (final ColumnFamilyHandle cfHandle : columnFamilies.values()) {
                cfHandle.getDescriptor().getOptions().close();
                cfHandle.close();
            }
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
        columnFamilies.clear();

        if (rocksDb != null) {
            rocksDb.close();
            rocksDb = null;
        }

        if (dbOptions != null) {
            dbOptions.close();
            dbOptions = null;
        }
    }

    @Override
    public byte[] get(final String table, final String key) throws RemoteException {
        try {
            return rocksDb.get(getColumnFamilyHandle(table), key.getBytes(UTF_8));
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    private ColumnFamilyHandle getColumnFamilyHandle(String table) throws RocksDBException {
        ColumnFamilyHandle cfHandle = columnFamilies.get(table);

        if (cfHandle == null) {
            cfHandle = createColumnFamily(table);
        }
        return cfHandle;
    }

    @Override
    public List<byte[]> bulkGet(final String table,
                                 final String startkey,
                                 final int recordcount) throws RemoteException {
        List<byte[]> values = new ArrayList<>();
        try (final RocksIterator iterator = rocksDb.newIterator(getColumnFamilyHandle(table))) {
            int iterations = 0;
            for (iterator.seek(startkey.getBytes(UTF_8));
                 iterator.isValid() && iterations < recordcount;
                 iterator.next()) {
                values.add(iterator.value());
                iterations++;
            }
            return values;
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void put(String table, String key, byte[] value) throws RemoteException {
        try {
            rocksDb.put(getColumnFamilyHandle(table), key.getBytes(UTF_8), value);
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String table, String key) throws RemoteException {
        try {
            rocksDb.delete(getColumnFamilyHandle(table), key.getBytes(UTF_8));
        } catch (RocksDBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new RemoteException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getColumnFamilyNames() {
        return new HashSet<>(columnFamilies.keySet());
    }

    private ColumnFamilyHandle createColumnFamily(final String name) throws RocksDBException {
        synchronized (name.intern()) {
            ColumnFamilyHandle cfHandle = columnFamilies.get(name);
            if (cfHandle != null) {
                return cfHandle;
            }

            cfHandle =
                    rocksDb.createColumnFamily(new ColumnFamilyDescriptor(name.getBytes(UTF_8), cfOptions));
            columnFamilies.put(name, cfHandle);
            return cfHandle;
        }
    }
}
