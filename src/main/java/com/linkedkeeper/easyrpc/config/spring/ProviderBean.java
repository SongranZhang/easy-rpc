package com.linkedkeeper.easyrpc.config.spring;

import com.linkedkeeper.easyrpc.config.api.ProviderConfig;
import com.linkedkeeper.easyrpc.config.api.ServerConfig;
import com.linkedkeeper.easyrpc.server.RpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProviderBean extends ProviderConfig implements InitializingBean, DisposableBean, ApplicationContextAware, BeanNameAware {

    /**
     * slf4j logger for this class
     */
    private Logger logger = LoggerFactory.getLogger(ProviderBean.class);

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

    /**
     * Using implements InitializingBean
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        propertiesInit();
        export();
    }

    /*
   * 组装相应的ServiceConfig
   */
    private void propertiesInit() {
        if (applicationContext != null) {
            if (getServerConfigs() == null) {
                Map<String, ServerConfig> protocolMaps = applicationContext.getBeansOfType(ServerConfig.class, false, false);
                if (!CollectionUtils.isEmpty(protocolMaps)) {
                    List<ServerConfig> protocolLists = new ArrayList(protocolMaps.values());
                    setServerConfigs(protocolLists);
                }
            }
        }
    }

    @Override
    public void destroy() throws Exception {
        logger.info("easy rpc destroy provider with beanName {}", beanName);
        // todo unexport
    }
}
