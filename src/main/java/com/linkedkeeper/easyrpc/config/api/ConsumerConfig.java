package com.linkedkeeper.easyrpc.config.api;

import com.linkedkeeper.easyrpc.client.RpcClient;
import org.apache.commons.lang3.StringUtils;

public class ConsumerConfig<T> extends AbstractInterfaceConfig {

    /**
     * 直连调用地址
     */
    protected String url;

    /**
     * 连接超时时间
     */
    protected int connectTimeout;

    private volatile transient T proxyIns = null;

    protected T refer() {
        if (proxyIns != null)
            return proxyIns;
        try {
            proxyIns = (T) getProxyClass().newInstance();
            initConnections();
        } catch (Exception e) {
            throw new RuntimeException("Build consumer proxy error!", e);
        }
        return proxyIns;
    }

    private void initConnections() {
        RpcClient client = (RpcClient) proxyIns;
        client.initClient(url, connectTimeout);
    }

    protected Class<?> getProxyClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        try {
            if (StringUtils.isNotBlank(interfaceClass)) {
                this.proxyClass = Class.forName(interfaceClass);
            } else {
                throw new Exception("consumer.interfaceId, null, interfaceId must be not null");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
        return proxyClass;
    }

    /**
     * Gets url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets url.
     *
     * @param url the url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Gets connect timeout.
     *
     * @return the connect timeout
     */
    public int getTimeout() {
        return connectTimeout;
    }

    /**
     * Sets connect timeout.
     *
     * @param connectTimeout the connect timeout
     */
    public void setTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
}
