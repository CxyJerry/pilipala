package com.jerry.pilipala.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;

@Slf4j
public class RequestUtil {
    private static final String[] HEADER_CONFIG = {
            "x-forwarded-for",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",

    };
    private static String serverIp;

    static {
        InetAddress ia = null;
        try {
            ia = InetAddress.getLocalHost();
            serverIp = ia.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
        String ip = null;
        for (String config : HEADER_CONFIG) {
            ip = request.getHeader(config);
            if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
                break;
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = serverIp;
        }
        return ip;
    }

    public static String getLocalHostAddress() {
        try {
            InetAddress backupHost = null;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isLoopbackAddress()) {
                        continue;
                    }
                    if (inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                    if (Objects.isNull(backupHost)) {
                        backupHost = inetAddress;
                    }
                }
            }
            return Objects.isNull(backupHost) ? InetAddress.getLocalHost().getHostAddress() : backupHost.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException("获取本机 ip 信息失败");
        }
    }

}