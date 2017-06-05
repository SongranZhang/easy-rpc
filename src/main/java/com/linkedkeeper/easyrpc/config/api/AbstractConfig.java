package com.linkedkeeper.easyrpc.config.api;

import java.io.Serializable;
import java.util.regex.Pattern;

public abstract class AbstractConfig implements Serializable {

    /**
     * 可用的字符串为：英文大小写，数字，横杆-，下划线_，点. 冒号:
     * !@#$*,;有特殊含义
     */
    protected Pattern NORMAL_COLON = Pattern.compile("^[a-zA-Z0-9\\-\\_\\.:]+$");


    /**
     * 匹配正常字符串
     *
     * @param configValue 配置项
     * @return 是否匹配，否表示有其他字符
     */
    protected boolean matchValue(Pattern pattern, String configValue) {
        return pattern.matcher(configValue).find();
    }

    /**
     * 检查字符串是否是正常值（含冒号），不是则抛出异常
     *
     * @param configKey   配置项
     * @param configValue 配置值
     */
    protected void checkNormalWithColon(String configKey, String configValue) throws Exception {
        checkPattern(configKey, configValue, NORMAL_COLON, "only allow a-zA-Z0-9 '-' '_' '.' ':'");
    }

    /**
     * 根据正则表达式检查字符串是否是正常值（含冒号），不是则抛出异常
     *
     * @param configKey   配置项
     * @param configValue 配置值
     * @param pattern     正则表达式
     * @param message     消息
     */
    protected void checkPattern(String configKey, String configValue, Pattern pattern, String message) throws Exception {
        if (configValue != null && !matchValue(pattern, configValue)) {
            throw new Exception("check pattern error.");
        }
    }
}
