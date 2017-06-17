package com.linkedkeeper.easyrpc.config.api;

import com.linkedkeeper.easyrpc.server.RpcServer;

public class ServerConfig extends AbstractInterfaceConfig {

    /**
     * 绑定的地址。是某个网卡，还是全部地址
     */
    private final String host = "127.0.0.1";

    /**
     * 监听端口
     */
    protected int port;

    /**
     * 服务端对象
     */
    private volatile transient RpcServer server = null;

    public void start() throws Exception {
        if (server == null) {
            server = new RpcServer(host + ":" + port);
        }
    }

    /**
     * Gets server.
     *
     * @return the server
     */
    public RpcServer getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
