package com.linkedkeeper.easyrpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectManager.class);
    private static volatile ConnectManager connectManager = new ConnectManager();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

    private CopyOnWriteArrayList<RpcClientHandler> connectedHandlers = new CopyOnWriteArrayList<RpcClientHandler>();
    private Map<InetSocketAddress, RpcClientHandler> connectedServerNodes = new ConcurrentHashMap<InetSocketAddress, RpcClientHandler>();

    private ReentrantLock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    protected long connectTimeoutMills = 6000;
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;

    private ConnectManager() {
    }

    public static ConnectManager getInstance() {
        return connectManager;
    }

    public void updateConnectedServer(List<String> allServerAddress) {
        if (CollectionUtils.isNotEmpty(allServerAddress)) {
            //update local serverNodes cache
            HashSet<InetSocketAddress> newAllServerNodeSet = new HashSet<InetSocketAddress>();
            for (int i = 0; i < allServerAddress.size(); ++i) {
                String[] array = allServerAddress.get(i).split(":");
                if (array.length == 2) { // Should check IP and port
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    final InetSocketAddress remotePeer = new InetSocketAddress(host, port);
                    newAllServerNodeSet.add(remotePeer);
                }
            }
            // Add new server node
            for (final InetSocketAddress serverNodeAddress : newAllServerNodeSet) {
                if (!connectedServerNodes.keySet().contains(serverNodeAddress)) {
                    connectServerNode(serverNodeAddress);
                }
            }
            // Close and remove invalid server nodes
            for (int i = 0; i < connectedHandlers.size(); ++i) {
                RpcClientHandler connectedServerHandler = connectedHandlers.get(i);
                SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
                if (!newAllServerNodeSet.contains(remotePeer)) {
                    LOGGER.info("Remove invalid server node " + remotePeer);
                    RpcClientHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(remotePeer);
                    connectedHandlers.remove(connectedServerHandler);
                }
            }
        } else { // No available server node ( All server nodes are down )
            LOGGER.error("No available server node. All server nodes are down !!!");
            clearConnectedServer();
        }
    }

    public void clearConnectedServer() {
        for (final RpcClientHandler connectedServerHandler : connectedHandlers) {
            SocketAddress remotePeer = connectedServerHandler.getRemotePeer();
            RpcClientHandler handler = connectedServerNodes.get(remotePeer);
            handler.close();
            connectedServerNodes.remove(connectedServerHandler);
        }
        connectedHandlers.clear();
    }

    public void reconnect(final RpcClientHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemotePeer());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.submit(new Runnable() {
            public void run() {
                Bootstrap b = new Bootstrap();
                b.group(eventLoopGroup)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(new RpcClientInitializer());
                connect(b, remotePeer);
            }
        });
    }

    private void connect(final Bootstrap b, final InetSocketAddress remotePeer) {
        final ChannelFuture connectFuture = b.connect(remotePeer);
        connectFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                LOGGER.info("connectFuture.channel close operationComplete. remote peer = " + remotePeer);
                future.channel().eventLoop().schedule(new Runnable() {
                    public void run() {
                        LOGGER.warn("Attempting to reconnect.");
                        clearConnectedServer();
                        connect(b, remotePeer);
                    }
                }, 3, TimeUnit.SECONDS);
            }
        });
        connectFuture.addListener(new ChannelFutureListener() {
            public void operationComplete(final ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    LOGGER.info("Successfully connect to remote server. remote peer = " + remotePeer);
                    RpcClientHandler handler = future.channel().pipeline().get(RpcClientHandler.class);
                    addHandler(handler);
                } else {
                    LOGGER.error("Failed to connect.", future.cause());
                }
            }
        });
    }

    private void addHandler(RpcClientHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getChannel().remoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            return connected.await(this.connectTimeoutMills, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public void connect(final String serverAddress) {
        List<String> allServerAddress = Arrays.asList(serverAddress.split(","));
        updateConnectedServer(allServerAddress);
    }

    public RpcClientHandler chooseHandler() {
        CopyOnWriteArrayList<RpcClientHandler> handlers = (CopyOnWriteArrayList<RpcClientHandler>) this.connectedHandlers.clone();
        int size = handlers.size();
        while (isRunning && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    handlers = (CopyOnWriteArrayList<RpcClientHandler>) this.connectedHandlers.clone();
                    size = handlers.size();
                }
            } catch (InterruptedException e) {
                LOGGER.error("Waiting for available node is interrupted! ", e);
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return handlers.get(index);
    }

    public void stop() {
        isRunning = false;
        for (int i = 0; i < connectedHandlers.size(); ++i) {
            RpcClientHandler connectedServerHandler = connectedHandlers.get(i);
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
