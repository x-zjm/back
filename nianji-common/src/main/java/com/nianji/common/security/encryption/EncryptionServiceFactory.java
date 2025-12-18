package com.nianji.common.security.encryption;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.exception.business.BusinessException;
import com.nianji.common.exception.system.CryptoException;
import com.nianji.common.security.encryption.impl.AesEncryptionService;
import com.nianji.common.security.encryption.impl.HashService;
import com.nianji.common.security.encryption.impl.RsaEncryptionService;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;

/**
 * 加密服务工厂
 */
@Component
public class EncryptionServiceFactory {

    private final Map<EncryptionAlgorithm, EncryptionService> services;

    @Autowired
    public EncryptionServiceFactory(
            AesEncryptionService aesService,
            RsaEncryptionService rsaService,
            HashService hashService) {
        services = new EnumMap<>(EncryptionAlgorithm.class);
        services.put(EncryptionAlgorithm.AES_GCM, aesService);
        services.put(EncryptionAlgorithm.AES_CBC_PKCS5, aesService);
        services.put(EncryptionAlgorithm.RSA_ECB_OAEP, rsaService);
        services.put(EncryptionAlgorithm.RSA_ECB_PKCS1, rsaService);
        services.put(EncryptionAlgorithm.SHA256, hashService);
        services.put(EncryptionAlgorithm.SHA512, hashService);
        services.put(EncryptionAlgorithm.MD5, hashService);
        services.put(EncryptionAlgorithm.BCRYPT, hashService);
        services.put(EncryptionAlgorithm.PBKDF2, hashService);
    }

    /**
     * 获取加密服务
     */
    public EncryptionService getService(EncryptionAlgorithm algorithm) {
        EncryptionService service = services.get(algorithm);
        if (service == null) {
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_ERROR);
        }
        return service;
    }

    /**
     * 获取默认的加密服务（AES-GCM）
     */
    public EncryptionService getDefaultService() {
        return getService(EncryptionAlgorithm.AES_GCM);
    }
}