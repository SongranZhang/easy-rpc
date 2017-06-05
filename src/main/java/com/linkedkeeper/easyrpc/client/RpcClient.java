package com.linkedkeeper.easyrpc.client;

import com.linkedkeeper.easyrpc.client.proxy.IAsyncObjectProxy;
import com.linkedkeeper.easyrpc.client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
    private static final Map<Class, Object> proxyInstances = new ConcurrentHashMap();

    private String serverAddress;

    public RpcClient(String serverAddress) {
        this.serverAddress = serverAddress;
        connect();
    }

    private void connect() {
        ConnectManager.getInstance().connect(this.serverAddress);
    }

    public static <T> T create(Class<T> interfaceClass) {
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

    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy(interfaceClass);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        ConnectManager.getInstance().stop();
    }

}
