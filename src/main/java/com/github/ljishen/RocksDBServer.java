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

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Signal;
import sun.misc.SignalHandler;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RocksDBServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(RocksDBServer.class);

    private static IRocksDB rocksDB;

    static int getRegistryPort(int port) throws RemoteException {
        if (port <= 0 || port > 65535) {
            throw new RemoteException("Invalid port number");
        }
        return port;
    }

    static String getRegistryHost(String host) throws RemoteException {
        if (!InternetDomainName.isValid(host) &&
                !InetAddresses.isInetAddress(host)) {
            throw new RemoteException("Invalid hostname");
        }
        return host;
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(
                (t, e) -> LOGGER.error("Error on running RocksDB RMI server: " + e.getMessage(), e));

        try {
            int registryPort = Registry.REGISTRY_PORT;
            if (args.length >= 1) {
                registryPort = getRegistryPort(Integer.parseInt(args[0].trim()));
            }

            String registryHost = "localhost";
            if (args.length >= 2) {
                registryHost = getRegistryHost(args[1].trim());
            }

            LOGGER.info("Starting RocksDB RMI server on " + registryHost + ":" + registryPort);

            System.setProperty("java.rmi.server.hostname", registryHost);

            // Store the reference of the RMI server object in a static field to prevent it
            // from being garbage collected and then unexported.
            // https://stackoverflow.com/questions/25872985/rmi-server-shuts-down-on-its-own
            rocksDB = new RocksDBImpl();

            IRocksDB stub = (IRocksDB) UnicastRemoteObject.exportObject(rocksDB, 0);

            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(registryPort);
            String name = "RocksDB-" + registryPort;
            registry.rebind(name, stub);

            final String[] exitSignals = {"INT", "TERM"};
            for (String es : exitSignals) {
                Signal.handle(new Signal(es), new SignalHandler() {
                    @Override
                    public void handle(Signal sig) {
                        LOGGER.warn("Received SIG" + sig.getName() + " indicating exit request");
                        System.exit(0);
                    }
                });
            }

            // Register a shutdown hook to clean up resources before exit.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    LOGGER.info("Removing the registry binding and the exported object from the RMI runtime...");
                    registry.unbind(name);
                    UnicastRemoteObject.unexportObject(rocksDB, true);
                    rocksDB.close();
                    LOGGER.info("RocksDB RMI server has been successfully shut down.");
                } catch (RemoteException | NotBoundException e) {
                    LOGGER.error("Fail to shutdown RocksDB RMI server: " + e.getMessage(), e);
                }
            }));

            LOGGER.info("Server is running.");
        } catch (RemoteException e) {
            LOGGER.error("Fail to start RocksDB RMI server: " + e.getMessage(), e);
        }
    }
}
