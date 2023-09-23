package com.example.mc_jacoco.util;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author luping
 * @date 2023/9/23 14:35
 */
@Slf4j
public class GetIPAddressUtil {

    /**
     * 获取服务器的IP地址
     *
     * @return
     */
    public static String getLinuxIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    // 拿到机器全部IP地址
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipAdress = inetAddress.getHostAddress();
                        if (!ipAdress.contains("::") && !ipAdress.contains("0:0") && !ipAdress.contains("fe80")) {
                            ip = ipAdress;
                        }
                    }
                }
            }
            return ip;
        } catch (SocketException e) {
            log.error("【获取服务IP地址出错...】【错误信息是：{}】", e.getMessage());
            throw new RuntimeException("获取服务IP地址出错...");
        }
    }
}

