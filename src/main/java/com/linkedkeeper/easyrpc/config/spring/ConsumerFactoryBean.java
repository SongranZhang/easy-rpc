package com.linkedkeeper.easyrpc.config.spring;

import com.linkedkeeper.easyrpc.config.api.ConsumerConfig;
import org.springframework.beans.factory.FactoryBean;

public class ConsumerFactoryBean<T> extends ConsumerConfig<T> implements FactoryBean<T> {

    private transient T bean = null;
    private transient Class<?> objectType = null;

    @Override
    public T getObject() throws Exception {
        bean = refer();
        return bean;
    }

    @Override
    public Class<?> getObjectType() {
        try {
            objectType = getProxyClass();
        } catch (Exception e) {
            objectType = null;
        }
        return objectType;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
