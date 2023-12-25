package com.example.mc_jacoco.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author luping
 * @date 2023/9/23 14:33
 */
@Slf4j
public class LocalIpUtil {

    /**
     * 组装本机服务器的IP
     * @return
     */
    public static String getBaseUrl(){
        StringBuilder sb = new StringBuilder();
        try {
            String localIp = GetIPAddressUtil.getLinuxIpAddress();
            sb.append("http://").append(localIp).append(":5001/");
            return sb.toString();
        } catch (Exception e) {
            log.error("【获取本机机器ip出现异常...】【异常原因：{}】",e.getMessage());
        }
        return sb.toString();
    }
}
