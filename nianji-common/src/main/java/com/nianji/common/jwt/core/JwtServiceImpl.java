package com.nianji.common.jwt.core;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.exception.ExceptionFactory;
import com.nianji.common.jwt.api.JwtGenerator;
import com.nianji.common.jwt.api.JwtValidator;
import com.nianji.common.jwt.config.JwtProperties;
import com.nianji.common.jwt.dto.JwtDetails;
import com.nianji.common.jwt.dto.JwtUserInfo;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtGenerator, JwtValidator {

    private final JwtProperties jwtProperties;

    private JWSSigner signer;
    private JWSVerifier verifier;

    @PostConstruct
    public void init() {
        try {
            this.signer = new MACSigner(jwtProperties.getSecret());
            this.verifier = new MACVerifier(jwtProperties.getSecret());
            log.info("JWT服务初始化成功");
        } catch (JOSEException e) {
            log.error("JWT服务初始化失败", e);
            throw ExceptionFactory.authService(
                    ErrorCode.System.AUTH_SERVICE_ERROR,
                    "JWT服务初始化失败",
                    e
            );
        }
    }

    private static final String CLAIM_KEY_USER_ID = "userId";
    private static final String CLAIM_KEY_TOKEN_TYPE = "tokenType";
    private static final String CLAIM_KEY_CSRF_TOKEN = "csrfToken";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String TOKEN_TYPE_SHORT = "short";

    @Override
    public String generateAccessToken(String username, Long userId) {
        return generateToken(username, userId, TOKEN_TYPE_ACCESS, jwtProperties.getExpiration(), null);
    }

    @Override
    public String generateAccessTokenWithCsrf(String username, Long userId, String csrfToken) {
        return generateToken(username, userId, TOKEN_TYPE_ACCESS, jwtProperties.getExpiration(), csrfToken);
    }

    @Override
    public String generateRefreshToken(String username, Long userId) {
        return generateToken(username, userId, TOKEN_TYPE_REFRESH, jwtProperties.getRefreshExpiration(), null);
    }

    @Override
    public String generateShortLivedToken(String username, Long userId, int minutes) {
        return generateToken(username, userId, TOKEN_TYPE_SHORT, minutes * 60L, null);
    }

    @Override
    public String generateCsrfToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Override
    public String validateAndRefresh(String refreshToken) {
        // 验证刷新令牌 - 客户端问题
        if (!validateToken(refreshToken)) {
            throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID,
                    "无效的刷新令牌"
            );
        }

        String tokenType = extractTokenType(refreshToken);
        if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
            throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID,
                    "非刷新令牌"
            );
        }

        try {
            String username = extractUsername(refreshToken);
            Long userId = extractUserId(refreshToken);
            return generateAccessToken(username, userId);
        } catch (Exception e) {
            log.error("令牌刷新失败", e);
            // 令牌刷新失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_REFRESH_FAILED,
                    "令牌刷新失败",
                    "refresh_token",
                    "refresh",
                    e
            );
        }
    }

    private String generateToken(String username, Long userId, String tokenType, long expiration, String csrfToken) {
        try {
            JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim(CLAIM_KEY_USER_ID, userId)
                    .claim(CLAIM_KEY_TOKEN_TYPE, tokenType)
                    .issuer(jwtProperties.getIssuer())
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expiration * 1000));

            if (csrfToken != null) {
                claimsBuilder.claim(CLAIM_KEY_CSRF_TOKEN, csrfToken);
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();

            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            log.debug("生成JWT令牌 - 用户: {}, 类型: {}, 有效期: {}秒", username, tokenType, expiration);
            return signedJWT.serialize();

        } catch (JOSEException e) {
            log.error("JWT令牌生成失败 - 用户: {}, 类型: {}", username, tokenType, e);
            // 令牌生成失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_GENERATION_FAILED,
                    "令牌生成失败",
                    tokenType,
                    "generate",
                    e
            );
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean validSignature = signedJWT.verify(verifier);
            boolean notExpired = new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime());

            if (!validSignature) {
                log.warn("JWT签名验证失败");
                return false;
            }
            if (!notExpired) {
                log.warn("JWT令牌已过期");
                return false;
            }

            return true;
        } catch (ParseException e) {
            log.debug("JWT令牌解析失败: {}", e.getMessage());
            return false;
        } catch (JOSEException e) {
            log.debug("JWT签名验证异常: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("JWT令牌验证失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateTokenWithCsrf(String token, String expectedCsrfToken) {
        if (!validateToken(token)) {
            return false;
        }
        String actualCsrfToken = extractCsrfToken(token);
        return actualCsrfToken != null && actualCsrfToken.equals(expectedCsrfToken);
    }

    @Override
    public JwtUserInfo validateAndGetUserInfo(String token) {
        // 验证令牌有效性 - 客户端问题
        if (!validateToken(token)) {
            throw ExceptionFactory.authentication(
                    ErrorCode.Client.TOKEN_INVALID,
                    "无效的令牌"
            );
        }

        try {
            return JwtUserInfo.builder()
                    .username(extractUsername(token))
                    .userId(extractUserId(token))
                    .issuedAt(extractIssuedAt(token))
                    .expiration(extractExpiration(token))
                    .tokenType(extractTokenType(token))
                    .csrfToken(extractCsrfToken(token))
                    .build();
        } catch (Exception e) {
            log.error("提取用户信息失败", e);
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取用户信息",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    @Override
    public String extractUsername(String token) {
        try {
            return extractClaims(token).getSubject();
        } catch (Exception e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取用户名",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    @Override
    public Long extractUserId(String token) {
        try {
            return extractClaims(token).getLongClaim(CLAIM_KEY_USER_ID);
        } catch (Exception e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取用户ID",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    @Override
    public String extractTokenType(String token) {
        try {
            return extractClaims(token).getStringClaim(CLAIM_KEY_TOKEN_TYPE);
        } catch (Exception e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取令牌类型",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    @Override
    public Date extractExpiration(String token) {
        try {
            return extractClaims(token).getExpirationTime();
        } catch (Exception e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取过期时间",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("检查令牌过期状态失败", e);
            return true;
        }
    }

    @Override
    public boolean isTokenAboutToExpire(String token, long time, TimeUnit timeUnit) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            long millisUntilExpiration = expiration.getTime() - now.getTime();
            long threshold = timeUnit.toMillis(time);
            return millisUntilExpiration > 0 && millisUntilExpiration <= threshold;
        } catch (Exception e) {
            log.error("检查令牌即将过期状态失败", e);
            return false;
        }
    }

    @Override
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            Date now = new Date();
            return expiration.getTime() - now.getTime();
        } catch (Exception e) {
            log.error("获取令牌剩余时间失败", e);
            return -1;
        }
    }

    @Override
    public JwtDetails getTokenDetails(String token) {
        try {
            JWTClaimsSet claims = extractClaims(token);
            return JwtDetails.builder()
                    .subject(claims.getSubject())
                    .userId(claims.getLongClaim(CLAIM_KEY_USER_ID))
                    .issuer(claims.getIssuer())
                    .issuedAt(claims.getIssueTime())
                    .expiration(claims.getExpirationTime())
                    .tokenType(claims.getStringClaim(CLAIM_KEY_TOKEN_TYPE))
                    .csrfToken(claims.getStringClaim(CLAIM_KEY_CSRF_TOKEN))
                    .remainingTime(getRemainingTime(token))
                    .expired(isTokenExpired(token))
                    .build();
        } catch (Exception e) {
            log.error("获取令牌详情失败", e);
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法解析令牌详情",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    private JWTClaimsSet extractClaims(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet();
        } catch (ParseException e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "令牌解析失败",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    private Date extractIssuedAt(String token) {
        try {
            return extractClaims(token).getIssueTime();
        } catch (Exception e) {
            // 令牌解析失败 - 服务端问题
            throw ExceptionFactory.authService(
                    ErrorCode.System.TOKEN_PARSING_ERROR,
                    "无法提取签发时间",
                    "JWT",
                    "parse",
                    e
            );
        }
    }

    private String extractCsrfToken(String token) {
        try {
            return extractClaims(token).getStringClaim(CLAIM_KEY_CSRF_TOKEN);
        } catch (Exception e) {
            log.debug("JWT中未找到CSRF token");
            return null;
        }
    }
}