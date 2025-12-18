package com.nianji.auth.model.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备分析结果记录类
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceAnalysisResult {
    private Boolean risk;
    private String trustLevel;
}