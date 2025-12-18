package com.nianji.auth.service.impl;

import com.nianji.auth.service.PasswordStrengthService;
import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.BizResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 密码强度验证服务
 */
@Slf4j
@Service
public class PasswordStrengthServiceImpl implements PasswordStrengthService {

    // 常见弱密码列表
    private static final Set<String> COMMON_WEAK_PASSWORDS = Set.of(
            "123456", "password", "12345678", "qwerty", "abc123",
            "password1", "12345", "123456789", "1234567", "123123",
            "111111", "000000", "admin", "welcome", "login", "passw0rd"
    );

    // 正则表达式模式
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?]");

    /**
     * 验证密码强度
     */
    public BizResult<Void> validatePasswordStrength(String password) {
        if (!StringUtils.hasText(password)) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码不能为空");
        }

        // 长度检查
        if (password.length() < 8) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码长度至少8位");
        }

        if (password.length() > 128) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码长度不能超过128位");
        }

        // 复杂度检查
        List<String> violations = new ArrayList<>();

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            violations.add("大写字母");
        }

        if (!LOWERCASE_PATTERN.matcher(password).find()) {
            violations.add("小写字母");
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            violations.add("数字");
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            violations.add("特殊字符");
        }

        if (!violations.isEmpty()) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR,
                    String.format("密码必须包含：%s", String.join("、", violations)));
        }

        // 弱密码检查
        BizResult<Void> weakPasswordCheck = checkWeakPassword(password);
        if (!weakPasswordCheck.isSuccess()) {
            return weakPasswordCheck;
        }

        // 密码熵检查
        if (calculatePasswordEntropy(password) < 3) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码强度不足，请使用更复杂的密码");
        }

        return BizResult.success();
    }

    /**
     * 检查是否为弱密码
     */
    private BizResult<Void> checkWeakPassword(String password) {
        // 检查常见弱密码
        if (COMMON_WEAK_PASSWORDS.contains(password.toLowerCase())) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码过于简单，请使用更复杂的密码");
        }

        // 检查连续字符
        if (hasSequentialCharacters(password, 4)) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码包含过多连续字符");
        }

        // 检查重复字符
        if (hasRepeatedCharacters(password, 4)) {
            return BizResult.fail(ErrorCode.Client.PARAM_ERROR, "密码包含过多重复字符");
        }

        return BizResult.success();
    }

    /**
     * 计算密码熵
     */
    private double calculatePasswordEntropy(String password) {
        int poolSize = 0;

        if (UPPERCASE_PATTERN.matcher(password).find()) poolSize += 26;
        if (LOWERCASE_PATTERN.matcher(password).find()) poolSize += 26;
        if (DIGIT_PATTERN.matcher(password).find()) poolSize += 10;
        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) poolSize += 20;

        // 如果无法确定字符池大小，使用保守估计
        if (poolSize == 0) poolSize = 26;

        return password.length() * (Math.log(poolSize) / Math.log(2));
    }

    /**
     * 检查连续字符
     */
    private boolean hasSequentialCharacters(String str, int maxSequential) {
        if (str.length() < maxSequential) return false;

        for (int i = 0; i <= str.length() - maxSequential; i++) {
            String substring = str.substring(i, i + maxSequential);
            if (isSequential(substring) || isKeyboardSequential(substring)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查重复字符
     */
    private boolean hasRepeatedCharacters(String str, int maxRepeated) {
        if (str.length() < maxRepeated) return false;

        for (int i = 0; i <= str.length() - maxRepeated; i++) {
            String substring = str.substring(i, i + maxRepeated);
            if (isRepeated(substring)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSequential(String str) {
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) - str.charAt(i - 1) != 1) {
                return false;
            }
        }
        return true;
    }

    private boolean isKeyboardSequential(String str) {
        // 简单的键盘序列检查
        String[] keyboardSequences = {
                "qwerty", "asdfgh", "zxcvbn", "123456"
        };

        String lowerStr = str.toLowerCase();
        for (String sequence : keyboardSequences) {
            if (sequence.contains(lowerStr)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRepeated(String str) {
        char firstChar = str.charAt(0);
        for (int i = 1; i < str.length(); i++) {
            if (str.charAt(i) != firstChar) {
                return false;
            }
        }
        return true;
    }
}