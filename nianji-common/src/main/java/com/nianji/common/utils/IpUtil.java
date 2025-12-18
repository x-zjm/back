package com.nianji.common.utils;


import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import jakarta.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;


/**
 * IP 工具类 提供 IP 地址获取、地理位置查询、IP 类型判断等功能
 */
@Slf4j
public class IpUtil {


    private IpUtil() {
        // 工具类，防止实例化
    }


    // ============ IP 地址相关常量 ============


    /**
     * 未知 IP
     */
    public static final String UNKNOWN_IP = "unknown";


    /**
     * 本地 IP
     */
    public static final String LOCALHOST_IP = "127.0.0.1";


    /**
     * IPv4 本地地址
     */
    public static final String LOCALHOST_IPV4 = "127.0.0.1";


    /**
     * IPv6 本地地址
     */
    public static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";


    /**
     * 默认分隔符
     */
    public static final String IP_SEPARATOR = ",";


    // ============ IP 头信息常量 ============


    /**
     * 可能包含真实 IP 的请求头
     */
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    };


    // ============ IP 地址模式匹配 ============


    /**
     * IPv4 地址正则表达式
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );


    /**
     * IPv6 地址正则表达式（简化版）
     */
    private static final Pattern IPV6_PATTERN = Pattern.compile(
            "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"
    );


    /**
     * 私有 IP 地址段
     */
    private static final String[] PRIVATE_IP_RANGES = {
            "10.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.",
            "172.25.", "172.26.", "172.27.", "172.28.", "172.29.",
            "172.30.", "172.31.", "192.168."
    };


    // ============ IP 地址获取方法 ============


    /**
     * 获取客户端 IP 地址 会依次检查 X-Forwarded-For、X-Real-IP 等头部信息
     *
     * @param request
     *         HttpServletRequest 对象
     * @return IP 地址
     */
    public static String getIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }


        String ip = null;


        // 1. 检查代理头信息
        for (String header : IP_HEADERS) {
            ip = request.getHeader(header);
            if (isValidIp(ip)) {
                break;
            }
        }


        // 2. 如果代理头中没有有效的 IP，使用 getRemoteAddr
        if (!isValidIp(ip)) {
            ip = request.getRemoteAddr();
        }


        // 3. 处理多个 IP 的情况（如 X-Forwarded-For: client, proxy1, proxy2）
        if (StrUtil.isNotEmpty(ip) && ip.contains(IP_SEPARATOR)) {
            ip = ip.split(IP_SEPARATOR)[0].trim();
        }


        // 4. 处理本地地址
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }


        return isValidIp(ip) ? ip : UNKNOWN_IP;
    }


    /**
     * 获取客户端 IP 地址（简化版） 直接从请求中获取，不检查代理头
     *
     * @param request
     *         HttpServletRequest 对象
     * @return IP 地址
     */
    public static String getSimpleIpAddr(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN_IP;
        }


        String ip = request.getRemoteAddr();
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST_IPV4;
        }


        return isValidIp(ip) ? ip : UNKNOWN_IP;
    }


    /**
     * 验证 IP 地址是否有效
     *
     * @param ip
     *         IP 地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        return StrUtil.isNotEmpty(ip) &&
                !UNKNOWN_IP.equalsIgnoreCase(ip) &&
                (isValidIpv4(ip) || isValidIpv6(ip));
    }


    /**
     * 验证是否为有效的 IPv4 地址
     *
     * @param ip
     *         IP 地址
     * @return 是否为 IPv4
     */
    public static boolean isValidIpv4(String ip) {
        return ip != null && IPV4_PATTERN.matcher(ip).matches();
    }


    /**
     * 验证是否为有效的 IPv6 地址
     *
     * @param ip
     *         IP 地址
     * @return 是否为 IPv6
     */
    public static boolean isValidIpv6(String ip) {
        return ip != null && IPV6_PATTERN.matcher(ip).matches();
    }


    // ============ IP 类型判断方法 ============


    /**
     * 判断是否为内网 IP
     *
     * @param ip
     *         IP 地址
     * @return 是否为内网 IP
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIpv4(ip)) {
            return false;
        }


        // 检查私有 IP 段
        for (String privateRange : PRIVATE_IP_RANGES) {
            if (ip.startsWith(privateRange)) {
                return true;
            }
        }


        // 检查回环地址
        return ip.startsWith("127.") || ip.equals(LOCALHOST_IPV4);
    }


    /**
     * 判断是否为公网 IP
     *
     * @param ip
     *         IP 地址
     * @return 是否为公网 IP
     */
    public static boolean isPublicIp(String ip) {
        return isValidIp(ip) && !isInternalIp(ip);
    }


    /**
     * 判断是否为本地 IP
     *
     * @param ip
     *         IP 地址
     * @return 是否为本地 IP
     */
    public static boolean isLocalhost(String ip) {
        return LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip);
    }


    // ============ IP 地理位置方法 ============


    /**
     * 获取 IP 地理位置（简化版） 基于 IP 地址段进行简单的地理位置判断
     *
     * @param ip
     *         IP 地址
     * @return 地理位置信息
     */
    public static String getLocation(String ip) {
        if (!isValidIp(ip) || isInternalIp(ip)) {
            return "内网IP";
        }


        try {
            // 这里可以使用本地的 IP 地址库或调用第三方服务
            // 以下是基于常见 IP 地址段的简单判断
            return getLocationByIpSegment(ip);
        } catch (Exception e) {
            log.warn("获取 IP 地理位置失败: {}", ip, e);
            return "未知";
        }
    }


    /**
     * 根据 IP 地址段获取地理位置（简化实现）
     *
     * @param ip
     *         IP 地址
     * @return 地理位置
     */
    private static String getLocationByIpSegment(String ip) {
        if (!isValidIpv4(ip)) {
            return "未知";
        }


        String[] segments = ip.split("\\.");
        if (segments.length != 4) {
            return "未知";
        }


        int firstSegment = Integer.parseInt(segments[0]);
        int secondSegment = Integer.parseInt(segments[1]);


        // 基于 IP 地址段的简单地理位置判断
        // 注意：这只是一个简化实现，实际项目中应该使用专业的 IP 地址库
        if (firstSegment == 1) {
            return "中国";
        } else if (firstSegment == 14) {
            return "中国";
        } else if (firstSegment == 27) {
            return "中国";
        } else if (firstSegment == 36) {
            return "中国";
        } else if (firstSegment == 39) {
            return "中国";
        } else if (firstSegment == 42) {
            return "中国";
        } else if (firstSegment == 49) {
            return "中国";
        } else if (firstSegment == 58) {
            return "中国";
        } else if (firstSegment == 59) {
            return "中国";
        } else if (firstSegment == 60) {
            return "中国";
        } else if (firstSegment == 61) {
            return "中国";
        } else if (firstSegment == 101) {
            return "中国";
        } else if (firstSegment == 106) {
            return "中国";
        } else if (firstSegment == 110) {
            return "中国";
        } else if (firstSegment == 111) {
            return "中国";
        } else if (firstSegment == 112) {
            return "中国";
        } else if (firstSegment == 113) {
            return "中国";
        } else if (firstSegment == 114) {
            return "中国";
        } else if (firstSegment == 115) {
            return "中国";
        } else if (firstSegment == 116) {
            return "中国";
        } else if (firstSegment == 117) {
            return "中国";
        } else if (firstSegment == 118) {
            return "中国";
        } else if (firstSegment == 119) {
            return "中国";
        } else if (firstSegment == 120) {
            return "中国";
        } else if (firstSegment == 121) {
            return "中国";
        } else if (firstSegment == 122) {
            return "中国";
        } else if (firstSegment == 123) {
            return "中国";
        } else if (firstSegment == 124) {
            return "中国";
        } else if (firstSegment == 125) {
            return "中国";
        } else if (firstSegment == 126) {
            return "中国";
        } else if (firstSegment == 171) {
            return "中国";
        } else if (firstSegment == 175) {
            return "中国";
        } else if (firstSegment == 180) {
            return "中国";
        } else if (firstSegment == 182) {
            return "中国";
        } else if (firstSegment == 183) {
            return "中国";
        } else if (firstSegment == 202) {
            return "中国";
        } else if (firstSegment == 203) {
            return "中国";
        } else if (firstSegment == 210) {
            return "中国";
        } else if (firstSegment == 211) {
            return "中国";
        } else if (firstSegment == 218) {
            return "中国";
        } else if (firstSegment == 219) {
            return "中国";
        } else if (firstSegment == 220) {
            return "中国";
        } else if (firstSegment == 221) {
            return "中国";
        } else if (firstSegment == 222) {
            return "中国";
        } else if (firstSegment >= 223 && firstSegment <= 239) {
            return "中国";
        } else {
            return "海外";
        }
    }


    /**
     * 获取详细的 IP 地理位置信息 实际项目中应该集成专业的 IP 地址查询服务
     *
     * @param ip
     *         IP 地址
     * @return 详细的地理位置信息
     */
    public static IpLocation getDetailedLocation(String ip) {
        IpLocation location = new IpLocation();
        location.setIp(ip);

        if (!isValidIp(ip)) {
            location.setCountry("未知");
            location.setRegion("未知");
            location.setCity("未知");
            location.setIsp("未知");
            return location;
        }


        if (isInternalIp(ip)) {
            location.setCountry("内网");
            location.setRegion("内网");
            location.setCity("内网");
            location.setIsp("内网");
            return location;
        }


        try {
            // 这里可以调用第三方 IP 地址查询 API
            // 例如：淘宝 IP、百度 IP、ip-api.com 等
            // 以下是模拟数据
            location.setCountry("中国");
            location.setRegion("北京市");
            location.setCity("北京市");
            location.setIsp("电信");

        } catch (Exception e) {
            log.warn("获取详细 IP 地理位置失败: {}", ip, e);
            location.setCountry("未知");
            location.setRegion("未知");
            location.setCity("未知");
            location.setIsp("未知");
        }


        return location;
    }


    // ============ IP 转换和计算 ============


    /**
     * 将 IP 地址转换为长整型
     *
     * @param ip
     *         IP 地址
     * @return 长整型表示的 IP
     */
    public static long ipToLong(String ip) {
        if (!isValidIpv4(ip)) {
            return 0;
        }


        String[] segments = ip.split("\\.");
        long result = 0;

        for (int i = 0; i < 4; i++) {
            result += Long.parseLong(segments[i]) << (24 - (8 * i));
        }

        return result;
    }


    /**
     * 将长整型转换为 IP 地址
     *
     * @param ipLong
     *         长整型 IP
     * @return IP 地址字符串
     */
    public static String longToIp(long ipLong) {
        StringBuilder ip = new StringBuilder();

        ip.append(ipLong >>> 24);
        ip.append(".");
        ip.append((ipLong >>> 16) & 0xFF);
        ip.append(".");
        ip.append((ipLong >>> 8) & 0xFF);
        ip.append(".");
        ip.append(ipLong & 0xFF);

        return ip.toString();
    }


    /**
     * 获取 IP 地址的整数表示（用于范围比较）
     *
     * @param ip
     *         IP 地址
     * @return 整数表示的 IP
     */
    public static int ipToInt(String ip) {
        return (int) (ipToLong(ip) & 0xFFFFFFFF);
    }


    // ============ 主机信息方法 ============


    /**
     * 获取本机 IP 地址
     *
     * @return 本机 IP 地址
     */
    public static String getLocalHostIp() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("获取本机 IP 地址失败", e);
            return LOCALHOST_IPV4;
        }
    }


    /**
     * 获取本机主机名
     *
     * @return 主机名
     */
    public static String getLocalHostName() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostName();
        } catch (UnknownHostException e) {
            log.error("获取本机主机名失败", e);
            return "unknown";
        }
    }


    // ============ IP 风险评估方法 ============


    /**
     * 评估 IP 风险等级
     *
     * @param ip
     *         IP 地址
     * @return 风险等级
     */
    public static RiskLevel assessIpRisk(String ip) {
        if (!isValidIp(ip)) {
            return RiskLevel.UNKNOWN;
        }


        if (isInternalIp(ip)) {
            return RiskLevel.LOW;
        }


        // 这里可以添加更多的风险评估逻辑
        // 例如：检查 IP 是否在黑名单中、是否来自高风险地区等

        return RiskLevel.NORMAL;
    }


    /**
     * 检查 IP 是否在指定的 IP 段内
     *
     * @param ip
     *         要检查的 IP
     * @param startIp
     *         起始 IP
     * @param endIp
     *         结束 IP
     * @return 是否在范围内
     */
    public static boolean isInRange(String ip, String startIp, String endIp) {
        if (!isValidIpv4(ip) || !isValidIpv4(startIp) || !isValidIpv4(endIp)) {
            return false;
        }


        long ipLong = ipToLong(ip);
        long startLong = ipToLong(startIp);
        long endLong = ipToLong(endIp);


        return ipLong >= startLong && ipLong <= endLong;
    }


    // ============ 内部类 ============


    /**
     * IP 地理位置信息
     */
    public static class IpLocation {
        private String ip;
        private String country;
        private String region;
        private String city;
        private String isp;
        private double latitude;
        private double longitude;


        // Getters and Setters
        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }


        @Override
        public String toString() {
            return String.format("%s %s %s %s", country, region, city, isp);
        }
    }


    /**
     * IP 风险等级
     */
    public enum RiskLevel {
        UNKNOWN("未知", 0),
        LOW("低风险", 1),
        NORMAL("正常", 2),
        MEDIUM("中风险", 3),
        HIGH("高风险", 4);


        private final String description;
        private final int level;


        RiskLevel(String description, int level) {
            this.description = description;
            this.level = level;
        }


        public String getDescription() {
            return description;
        }

        public int getLevel() {
            return level;
        }
    }


    // ============ 工具方法 ============


    /**
     * 获取 IP 地址的匿名化版本（用于日志记录）
     *
     * @param ip
     *         IP 地址
     * @return 匿名化 IP
     */
    public static String anonymizeIp(String ip) {
        if (!isValidIpv4(ip)) {
            return ip;
        }


        String[] segments = ip.split("\\.");
        if (segments.length == 4) {
            return segments[0] + "." + segments[1] + "." + segments[2] + ".xxx";
        }


        return ip;
    }


    /**
     * 获取 IP 地址的哈希值（用于匿名化存储）
     *
     * @param ip
     *         IP 地址
     * @return 哈希值
     */
    public static String hashIp(String ip) {
        if (!isValidIp(ip)) {
            return "unknown";
        }


        // 使用简单的哈希算法，实际项目中可以使用更安全的哈希
        return Integer.toHexString(ip.hashCode());
    }
}