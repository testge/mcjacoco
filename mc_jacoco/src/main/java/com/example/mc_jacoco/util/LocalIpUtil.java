package com.example.mc_jacoco.util;

import lombok.extern.slf4j.Slf4j;

/**
 * @author luping
 * @date 2023/9/23 14:33
 */
@Slf4j
public class LocalIpUtil {

    public static String getBaseUrl(){
        StringBuilder sb = new StringBuilder();
        try {
            String localIp = GetIPAddressUtil.getLinuxIpAddress();
            sb.append("http://").append(localIp).append(":8081/");
            return sb.toString();
        } catch (Exception e) {
            log.error("【获取本机机器ip出现异常...】【异常原因：{}】",e.getMessage());
        }
        return sb.toString();
    }
}
