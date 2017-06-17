package com.linkedkeeper.easyrpc.config.spring;

import com.linkedkeeper.easyrpc.config.api.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class ServerBean extends ServerConfig implements InitializingBean, DisposableBean, BeanNameAware {

    /**
     * slf4j logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(ServerBean.class);

    private transient String beanName = null;

    /**
     * @param name
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void destroy() throws Exception {
        logger.info("Stop server with beanName {}", beanName);
    }
}
