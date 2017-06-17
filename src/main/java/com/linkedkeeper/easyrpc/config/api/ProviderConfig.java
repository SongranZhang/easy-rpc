package com.linkedkeeper.easyrpc.config.api;

import com.linkedkeeper.easyrpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProviderConfig extends AbstractInterfaceConfig {

    /**
     * slf4j logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(ProviderConfig.class);

    /**
     * 接口实现类引用
     */
    protected transient Object ref;

    /**
     * 配置的协议列表
     */
    private List<ServerConfig> serverConfigs = null;

    /**
     * 是否已发布
     */
    protected volatile boolean exported = false;

    /**
     * 发布服务
     *
     * @throws Exception the init error exception
     */
    protected void export() throws Exception {
        if (!exported) {
            for (ServerConfig serverConfig : serverConfigs) {
                try {
                    serverConfig.start();
                    // 注册接口
                    RpcServer server = serverConfig.getServer();
                    server.registerProcessor(this);
                } catch (Exception e) {
                    logger.error("Catch exception server.", e);
                }
            }
            exported = true;
        }
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public Object getRef() {
        return ref;
    }

    /**
     * Sets ref.
     *
     * @param ref the ref
     */
    public void setRef(Object ref) {
        this.ref = ref;
    }

    /**
     * Gets serverConfigs.
     *
     * @return the serverConfigs
     */
    public List<ServerConfig> getServerConfigs() {
        return serverConfigs;
    }

    /**
     * Sets serverConfigs.
     *
     * @param serverConfigs the serverConfigs
     */
    public void setServerConfigs(List<ServerConfig> serverConfigs) {
        this.serverConfigs = serverConfigs;
    }

}
