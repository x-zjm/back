package com.nianji.auth.service;

import com.nianji.auth.model.device.DeviceInfo;
import com.nianji.auth.model.device.DeviceTrustLevel;

import java.util.List;

/**
 * 设备管理服务 - 专注设备信息管理
 */
public interface DeviceManagementService {
    
    /**
     * 记录设备登录信息
     *
     * @param userId 用户ID
     * @param ip IP地址
     * @param userAgent 用户代理
     * @return 设备信息
     */
    DeviceInfo recordDeviceLogin(Long userId, String ip, String userAgent);
    
    /**
     * 获取用户设备历史
     *
     * @param userId 用户ID
     * @return 设备信息列表
     */
    List<DeviceInfo> getUserDevices(Long userId);
    
    /**
     * 分析设备信任级别
     *
     * @param deviceInfo 设备信息
     * @return 信任级别
     */
    DeviceTrustLevel analyzeDeviceTrustLevel(DeviceInfo deviceInfo);
    
    /**
     * 检查设备变更风险
     *
     * @param userId 用户ID
     * @param currentDevice 当前设备信息
     * @return 是否存在风险
     */
    boolean checkDeviceChangeRisk(Long userId, DeviceInfo currentDevice);
    
    /**
     * 标记设备为可信
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     */
    void markDeviceAsTrusted(Long userId, String deviceFingerprint);
    
    /**
     * 撤销设备信任
     *
     * @param userId 用户ID
     * @param deviceFingerprint 设备指纹
     */
    void revokeDeviceTrust(Long userId, String deviceFingerprint);
}