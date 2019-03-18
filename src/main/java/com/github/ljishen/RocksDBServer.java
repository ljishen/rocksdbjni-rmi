package com.github.ljishen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RocksDBServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDBServer.class);

    public static void main(String[] args) {
        try {
            int registryPort = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                registryPort = Integer.parseInt(args[0].trim());
            }

            String registryHost = "localhost";
            if (args.length >= 2) {
                registryHost = args[1].trim();
            }

            System.setProperty("java.rmi.server.hostname", registryHost);

            IRocksDB rocksDB = new RocksDBImpl();
            IRocksDB stub = (IRocksDB) UnicastRemoteObject.exportObject(rocksDB, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(registryPort);
            String name = "RocksDB-" + registryPort;
            registry.rebind(name, stub);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    registry.unbind(name);
                    UnicastRemoteObject.unexportObject(rocksDB, true);
                    LOGGER.info("RocksDB RMI server has been successfully shut down.");
                } catch (RemoteException | NotBoundException e) {
                    LOGGER.error("Fail to shutdown RocksDB RMI server: " + e.getMessage(), e);
                }
            }));

            LOGGER.info("RocksDB RMI server is running on " +
                    registryHost + ":" + registryPort);
        } catch (RemoteException e) {
            LOGGER.error("Fail to start RocksDB RMI server: " + e.getMessage(), e);
        }
    }
}
