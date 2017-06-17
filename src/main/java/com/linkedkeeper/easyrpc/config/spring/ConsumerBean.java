package com.linkedkeeper.easyrpc.config.spring;

import com.linkedkeeper.easyrpc.client.RpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ConsumerBean extends ConsumerFactoryBean implements InitializingBean, DisposableBean, ApplicationContextAware, BeanNameAware {

    /**
     * slf4j logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(ConsumerBean.class);

    protected transient ApplicationContext applicationContext = null;
    private transient String beanName = null;

    /**
     * @param name
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     * <p>
     * 不支持Spring3.2以下版本, 无法通过addApplicationListener启动export
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void destroy() throws Exception {
        logger.info("easy rpc destroy consumer with beanName {}", beanName);
    }

}
