package com.nianji.auth.controller;

import com.nianji.auth.service.PasswordTransmissionService;
import com.nianji.common.reqres.BizResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 认证业务安全密钥接口
 */
@Slf4j
@RestController
@Profile({"dev", "test", "local"})
@RequestMapping("/auth/security/test")
@RequiredArgsConstructor
public class TestSecurityKeyController {

    private final PasswordTransmissionService passwordTransmissionService;

    /**
     * 健康检查 - 验证认证业务加密服务状态
     */
    @GetMapping("/health")
    public BizResult<Map<String, String>> healthCheck(@RequestParam String data) {
        try {
            log.debug("认证业务加密服务健康检查通过");
            // 执行实际的健康检查
            String healthResult = passwordTransmissionService.healthCheck(data);
            return BizResult.success(Map.of("result", healthResult));
        } catch (Exception e) {
            log.error("认证业务加密服务健康检查异常", e);
            return BizResult.fail("SECURITY_ERROR", "加密服务异常: " + e.getMessage());
        }
    }
}