package com.linkedkeeper.easyrpc.config.api;

public abstract class AbstractInterfaceConfig extends AbstractIdConfig {

    /**
     * 不管普通调用和泛化调用，都是设置实际的接口类名称
     */
    protected String interfaceClass = null;

    /**
     * 服务别名= "group::version"
     */
    protected String alias = "";

    /**
     * 代理接口类，和T对应，主要针对泛化调用
     */
    protected volatile transient Class<?> proxyClass = null;

    /**
     * Gets interface.
     *
     * @return the interface
     */
    public String getInterface() {
        return interfaceClass;
    }

    /**
     * Sets interface.
     *
     * @param interfaceClass the interface
     */
    public void setInterface(String interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    /**
     * Gets alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets alias.
     *
     * @param alias the alias
     */
    public void setAlias(String alias) throws Exception {
        checkNormalWithColon("alias", alias);
        this.alias = alias;
    }

}
