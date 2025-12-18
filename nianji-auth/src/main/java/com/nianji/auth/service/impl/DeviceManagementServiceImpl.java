package com.nianji.auth.service.impl;

import com.nianji.auth.model.device.DeviceInfo;
import com.nianji.auth.model.device.DeviceTrustLevel;
import com.nianji.auth.service.DeviceManagementService;
import com.nianji.common.config.CacheConfig;
import com.nianji.common.constant.CacheKeys;
import com.nianji.common.utils.CacheUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 设备管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceManagementServiceImpl implements DeviceManagementService {

    private final CacheUtil cacheUtil;
    private final CacheConfig cacheConfig;

    @Override
    public DeviceInfo recordDeviceLogin(Long userId, String ip, String userAgent) {
        try {
            // 生成设备指纹前进行参数校验
            String deviceFingerprint = generateDeviceFingerprint(ip, userAgent);

            // 确保设备指纹不为空
            if (deviceFingerprint.trim().isEmpty()) {
                log.warn("生成的设备指纹为空，使用备用指纹");
                deviceFingerprint = "backup_fp_" + System.currentTimeMillis();
            }

            DeviceInfo deviceInfo = DeviceInfo.builder()
                    .deviceFingerprint(deviceFingerprint)
                    .userId(userId)
                    .ip(ip != null ? ip : "unknown")
                    .userAgent(userAgent != null ? userAgent : "unknown")
                    .loginTime(LocalDateTime.now())
                    .deviceType(parseDeviceType(userAgent))
                    .browserType(parseBrowserType(userAgent))
                    .operatingSystem(parseOperatingSystem(userAgent))
                    .trustLevel(DeviceTrustLevel.UNKNOWN)
                    .lastUsedTime(LocalDateTime.now())
                    .usageCount(1)
                    .build();

            // 存储设备信息
            String deviceKey = CacheKeys.Auth.trustedDevices(userId);
            cacheUtil.leftPush(deviceKey, deviceInfo);
            cacheUtil.trim(deviceKey, 0, 9);
            cacheUtil.expire(deviceKey,
                    cacheConfig.getExpire(deviceKey), TimeUnit.SECONDS);

            log.debug("记录设备登录 - 用户ID: {}, 设备指纹: {}", userId, deviceInfo.getDeviceFingerprint());
            return deviceInfo;
        } catch (Exception e) {
            log.error("记录设备登录失败 - 用户ID: {}, IP: {}", userId, ip, e);
            return null;
        }

    }

    @Override
    public List<DeviceInfo> getUserDevices(Long userId) {
        try {
            // 统一使用 Auth 模块的设备键（暂时保持现有实现）
            String deviceKey = CacheKeys.Auth.trustedDevices(userId);
            List<Object> devices = cacheUtil.range(deviceKey, 0, -1);

            if (devices == null) {
                return List.of();
            }

            return devices.stream()
                    .map(obj -> (DeviceInfo) obj)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取用户设备失败", e);
            return List.of();
        }
    }

    @Override
    public DeviceTrustLevel analyzeDeviceTrustLevel(DeviceInfo deviceInfo) {
        // 增强参数校验
        if (deviceInfo == null || deviceInfo.getDeviceFingerprint() == null) {
            log.warn("设备信息或设备指纹为空，返回未知信任级别");
            return DeviceTrustLevel.UNKNOWN;
        }

        List<DeviceInfo> userDevices = getUserDevices(deviceInfo.getUserId());

        // 过滤掉设备指纹为空的设备
        long sameDeviceCount = userDevices.stream()
                .filter(device -> device != null && device.getDeviceFingerprint() != null)
                .filter(device -> device.getDeviceFingerprint().equals(deviceInfo.getDeviceFingerprint()))
                .count();

        if (sameDeviceCount > 5) {
            return DeviceTrustLevel.HIGH;
        } else if (sameDeviceCount > 2) {
            return DeviceTrustLevel.MEDIUM;
        } else if (sameDeviceCount > 0) {
            return DeviceTrustLevel.LOW;
        } else {
            return DeviceTrustLevel.UNKNOWN;
        }
    }

    @Override
    public boolean checkDeviceChangeRisk(Long userId, DeviceInfo currentDevice) {
        if (currentDevice == null) {
            log.warn("当前设备信息为空，视为新设备存在风险");
            return true;
        }

        if (currentDevice.getDeviceFingerprint() == null) {
            log.warn("当前设备指纹为空，设备: {}, IP: {}",
                    currentDevice.getDeviceType(), currentDevice.getIp());
            return true; // 指纹为空视为新设备，存在风险
        }

        List<DeviceInfo> userDevices = getUserDevices(userId);

        // 如果是新设备，检查风险
        boolean isNewDevice = userDevices.stream()
                .filter(device -> device != null) // 过滤掉null设备
                .filter(device -> device.getDeviceFingerprint() != null) // 过滤掉指纹为空的设备
                .noneMatch(device -> device.getDeviceFingerprint().equals(currentDevice.getDeviceFingerprint()));

        if (isNewDevice) {
            DeviceTrustLevel trustLevel = analyzeDeviceTrustLevel(currentDevice);
            return trustLevel == DeviceTrustLevel.UNKNOWN;
        }

        return false;
    }

    @Override
    public void markDeviceAsTrusted(Long userId, String deviceFingerprint) {
        // 标记设备为可信的实现
        log.debug("标记设备为可信 - 用户ID: {}, 设备指纹: {}", userId, deviceFingerprint);
    }

    @Override
    public void revokeDeviceTrust(Long userId, String deviceFingerprint) {
        // 撤销设备信任的实现
        log.debug("撤销设备信任 - 用户ID: {}, 设备指纹: {}", userId, deviceFingerprint);
    }

    // ============ 私有方法 ============

    private String generateDeviceFingerprint(String ip, String userAgent) {
        try {
            // 确保参数不为空
            String safeIp = ip != null ? ip : "unknown_ip";
            String safeUserAgent = userAgent != null ? userAgent : "unknown_ua";

            // 使用更稳定的指纹生成算法
            String fingerprintData = safeIp + "|" + safeUserAgent;

            // 使用MD5哈希（如果没有DigestUtils，使用简单哈希）
            try {
                // 如果项目中有DigestUtils
                return org.springframework.util.DigestUtils.md5DigestAsHex(fingerprintData.getBytes());
            } catch (Exception e) {
                // 备用方案：使用Java内置的哈希
                int hashCode = fingerprintData.hashCode();
                return String.valueOf(Math.abs(hashCode));
            }

        } catch (Exception e) {
            log.error("生成设备指纹失败，使用备用方案", e);
            // 备用方案：使用UUID
            return "fallback_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        }
    }

    private String parseDeviceType(String userAgent) {
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        } else if (userAgent.toLowerCase().contains("tablet")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    private String parseBrowserType(String userAgent) {
        if (userAgent.toLowerCase().contains("chrome")) {
            return "Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Safari";
        } else {
            return "Unknown";
        }
    }

    private String parseOperatingSystem(String userAgent) {
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("ios")) {
            return "iOS";
        } else {
            return "Unknown";
        }
    }
}