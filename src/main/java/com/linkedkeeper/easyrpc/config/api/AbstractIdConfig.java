package com.linkedkeeper.easyrpc.config.api;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractIdConfig extends AbstractConfig {

    /**
     * Id生成器
     */
    private AtomicInteger ID_GENERATOR = new AtomicInteger(0);

    /**
     * Spring的BeanId
     */
    protected String id = null;

    /**
     * Gets id.
     *
     * @return the id
     */
    private String getId() {
        if (id == null) {
            id = "easyrpc-cfg-gen-" + ID_GENERATOR.getAndIncrement();
        }
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    private void setId(String id) {
        this.id = id;
    }
}
