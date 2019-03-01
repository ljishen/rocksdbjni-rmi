package org.rocksdb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RocksDBServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDBServer.class);

    public static void main(String[] args) {
        RocksDBImpl rocksDbImpl = new RocksDBImpl();

        try {
            IRocksDB stub = (IRocksDB) UnicastRemoteObject.exportObject(rocksDbImpl, 0);

            int port = Registry.REGISTRY_PORT;
            if (args.length == 1) {
                port = Integer.parseInt(args[0]);
            }

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry(port);
            registry.rebind("RocksDB", stub);

            LOGGER.info("RocksDB RMI Server is ready");
        } catch (RemoteException e) {
            LOGGER.error("RocksDB RMI Server error: " + e.getMessage(), e);
        }
    }
}
