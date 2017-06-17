package com.linkedkeeper.easyrpc.config.spring.schema;

import com.linkedkeeper.easyrpc.config.spring.ConsumerBean;
import com.linkedkeeper.easyrpc.config.spring.ProviderBean;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * Title: 继承NamespaceHandlerSupport，将xml的标签绑定到解析器
 * <p/>
 * Description: 在META-INF下增加spring.handlers和spring.schemas
 */
public class EasyRpcNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("provider", new EasyRpcBeanDefinitionParser(ProviderBean.class, true));
        registerBeanDefinitionParser("consumer", new EasyRpcBeanDefinitionParser(ConsumerBean.class, true));
    }
}
