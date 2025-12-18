package com.nianji.common.security.encryption.impl;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.security.encryption.EncryptionService;
import com.nianji.common.security.enums.EncryptionAlgorithm;
import com.nianji.common.exception.system.CryptoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA加密服务实现
 */
@Slf4j
@Component
public class RsaEncryptionService implements EncryptionService {

    private static final String RSA_ALGORITHM = "RSA";
    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public String encrypt(String data, String publicKey) throws CryptoException {
        try {
            PublicKey key = loadPublicKey(publicKey);
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.RSA_ECB_OAEP.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, key, secureRandom);
            
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            log.error("RSA加密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.ENCRYPT_FAILED);
        }
    }

    @Override
    public String decrypt(String encryptedData, String privateKey) throws CryptoException {
        try {
            PrivateKey key = loadPrivateKey(privateKey);
            Cipher cipher = Cipher.getInstance(EncryptionAlgorithm.RSA_ECB_OAEP.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, key, secureRandom);
            
            byte[] decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("RSA解密失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.DECRYPT_FAILED);
        }
    }

    @Override
    public String generateKey() throws CryptoException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(EncryptionAlgorithm.RSA_ECB_OAEP.getKeySize(), secureRandom);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            // 返回公钥，私钥需要单独保存
            return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_GENERATE_FAILED);
        }
    }

    /**
     * 生成完整的密钥对
     */
    public KeyPair generateKeyPair() throws CryptoException {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(EncryptionAlgorithm.RSA_ECB_OAEP.getKeySize(), secureRandom);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.CRYPTO_GENERATE_FAILED);
        }
    }

    @Override
    public EncryptionAlgorithm getAlgorithm() {
        return EncryptionAlgorithm.RSA_ECB_OAEP;
    }

    @Override
    public boolean verify(String data, String signature, String publicKey) throws CryptoException {
        try {
            PublicKey key = loadPublicKey(publicKey);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            return sig.verify(Base64.getDecoder().decode(signature));
        } catch (Exception e) {
            log.error("RSA签名验证失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.SIGN_VERIFY_FAILED);
        }
    }

    /**
     * 签名数据
     */
    public String sign(String data, String privateKey) throws CryptoException {
        try {
            PrivateKey key = loadPrivateKey(privateKey);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(key, secureRandom);
            sig.update(data.getBytes(StandardCharsets.UTF_8));
            byte[] signature = sig.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception e) {
            log.error("RSA签名失败", e);
            throw ExceptionFactory.crypto(ErrorCode.System.SIGN_GENERATE_FAILED);
        }
    }

    private PublicKey loadPublicKey(String publicKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePublic(keySpec);
    }

    private PrivateKey loadPrivateKey(String privateKeyStr) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        return keyFactory.generatePrivate(keySpec);
    }
}