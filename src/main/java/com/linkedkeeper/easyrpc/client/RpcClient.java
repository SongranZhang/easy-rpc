package com.linkedkeeper.easyrpc.client;

import com.linkedkeeper.easyrpc.client.proxy.IAsyncObjectProxy;
import com.linkedkeeper.easyrpc.client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcClient {

    private final Map<Class, Object> proxyInstances = new ConcurrentHashMap();

    private String serverAddress;

    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
        connect();
    }

    private void connect() {
        ConnectManager.getInstance().connect(this.serverAddress);
    }

    public <T> T create(Class<T> interfaceClass) {
        if (proxyInstances.containsKey(interfaceClass)) {
            return (T) proxyInstances.get(interfaceClass);
        } else {
            Object proxy = Proxy.newProxyInstance(
                    interfaceClass.getClassLoader(),
                    new Class<?>[]{interfaceClass},
                    new ObjectProxy(interfaceClass)
            );
            proxyInstances.put(interfaceClass, proxy);
            return (T) proxy;
        }
    }

    public <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy(interfaceClass);
    }

    public void stop() {
        ConnectManager.getInstance().stop();
    }

}
