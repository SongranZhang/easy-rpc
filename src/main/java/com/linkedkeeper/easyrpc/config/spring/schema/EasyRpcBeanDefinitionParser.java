package com.linkedkeeper.easyrpc.config.spring.schema;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class EasyRpcBeanDefinitionParser implements BeanDefinitionParser {

    private final Class<?> beanClass;
    private final boolean required;

    public EasyRpcBeanDefinitionParser(Class<?> beanClass, boolean required) {
        this.beanClass = beanClass;
        this.required = required;
    }

    /**
     * Method Name parse
     *
     * @param element
     * @param parserContext
     * @return Return Type BeanDefinition
     */
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        return parse(element, parserContext, this.beanClass, this.required);
    }


    private BeanDefinition parse(Element element, ParserContext parserContext, Class<?> beanClass, boolean required) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String id = element.getAttribute("id");
        if (StringUtils.isBlank(id) && required) {
            throw new IllegalStateException("This bean do not set spring bean id " + id);
        }

        // id肯定是必须的所以此处去掉对id是否为空的判断
        if (required) {
            if (parserContext.getRegistry().containsBeanDefinition(id)) {
                throw new IllegalStateException("Duplicate spring bean id " + id);
            }
            parserContext.getRegistry().registerBeanDefinition(id, beanDefinition);
        }

        // set各个属性
        for (Method setter : beanClass.getMethods()) {
            if (isProperty(setter, beanClass)) {
                String name = setter.getName();
                String property = name.substring(3, 4).toLowerCase() + name.substring(4);
                String value = element.getAttribute(property).trim();
                Object reference = value;
                //根据property名称来进行区别处理
                switch (property) {
                    case "protocol":
                    case "interface":
                        if (StringUtils.isNotBlank(value)) {
                            beanDefinition.getPropertyValues().addPropertyValue(property, reference);
                        }
                        break;
                    case "ref":
                        if (StringUtils.isNotBlank(value)) {
                            BeanDefinition refBean = parserContext.getRegistry().getBeanDefinition(value);
                            if (!refBean.isSingleton()) {
                                throw new IllegalStateException("The exported service ref " + value + " must be singleton! Please set the " + value + " bean scope to singleton, eg: <bean id=" + value + " scope=singleton ...>");
                            }
                            reference = new RuntimeBeanReference(value);
                        } else {
                            // 保持ref的null值
                            reference = null;
                        }
                        beanDefinition.getPropertyValues().addPropertyValue(property, reference);
                        break;
                    case "server":
                        parseMultiRef(property, value, beanDefinition);
                        break;
                    default:
                        // 默认非空字符串只是绑定值到属性
                        if (StringUtils.isNotBlank(value)) {
                            beanDefinition.getPropertyValues().addPropertyValue(property, reference);
                        }
                        break;
                }
            }
        }
        return beanDefinition;
    }

    private void parseMultiRef(String property, String value, RootBeanDefinition beanDefinition) {
        String[] values = value.split("\\s*[,]+\\s*");
        ManagedList<BeanReference> list = null;
        for (String v : values) {
            if (StringUtils.isNotBlank(v)) {
                if (list == null) {
                    list = new ManagedList();
                }
                list.add(new RuntimeBeanReference(v));
            }
        }
        beanDefinition.getPropertyValues().addPropertyValue(property, list);
    }

    /**
     * 判断是否有相应get\set方法的property
     */
    protected boolean isProperty(Method method, Class beanClass) {
        String methodName = method.getName();
        boolean flag = methodName.length() > 3 && methodName.startsWith("set") && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 1;
        Method getter = null;
        if (flag) {
            Class<?> type = method.getParameterTypes()[0];
            try {
                getter = beanClass.getMethod("get" + methodName.substring(3), new Class<?>[0]);
            } catch (NoSuchMethodException e) {
                try {
                    getter = beanClass.getMethod("is" + methodName.substring(3), new Class<?>[0]);
                } catch (NoSuchMethodException e2) {
                }
            }
            flag = getter != null && Modifier.isPublic(getter.getModifiers()) && type.equals(getter.getReturnType());
        }
        return flag;
    }
}
