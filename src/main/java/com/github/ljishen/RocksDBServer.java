package com.github.ljishen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RocksDBServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDBServer.class);

    public static void main(String[] args) {
        try {
            IRocksDB stub = (IRocksDB) UnicastRemoteObject.exportObject(new RocksDBImpl(), 0);

            int registryPort = Registry.REGISTRY_PORT;
            if (args.length == 1) {
                registryPort = Integer.parseInt(args[0]);
            }

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(registryPort);
            registry.rebind("RocksDB-" + registryPort, stub);

            LOGGER.info("RocksDB RMI Server is ready on port " + registryPort);
        } catch (RemoteException e) {
            LOGGER.error("RocksDB RMI Server error: " + e.getMessage(), e);
        }
    }
}
