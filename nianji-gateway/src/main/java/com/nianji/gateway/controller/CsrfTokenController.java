package com.nianji.gateway.controller;

import com.nianji.common.errorcode.ErrorCode;
import com.nianji.common.reqres.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CsrfTokenController {

    /**
     * 使用 Reactive 方式获取 CSRF Token
     */
    @GetMapping(value = "/api/csrf-token", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<Void>> getCsrfToken(ServerWebExchange exchange) {
        // 正确的方式：从 exchange 属性中获取 CsrfToken
        Mono<CsrfToken> csrfTokenMono = exchange.getAttribute(CsrfToken.class.getName());

        if (csrfTokenMono == null) {
            // 如果没有找到 CsrfToken，返回一个空的 Mono
            csrfTokenMono = Mono.empty();
        }

        return csrfTokenMono
                .defaultIfEmpty(createEmptyCsrfToken())
                .map(csrfToken -> {
                    // Map<String, Object> response = new HashMap<>();
                    // response.put("status", "success");
                    // response.put("timestamp", Instant.now().toString());
                    if (csrfToken != null && csrfToken.getToken() != null && !csrfToken.getToken().isEmpty()) {
                        // response.put("token", csrfToken.getToken());
                        // response.put("headerName", csrfToken.getHeaderName());
                        // response.put("parameterName", csrfToken.getParameterName());
                        // response.put("message", "CSRF token retrieved successfully");
                        log.debug("CSRF token retrieved: {}", csrfToken.getToken());
                        return Result.success();
                    } else {
                        // response.put("message", "CSRF token is not available. Using manual fallback.");
                        // response.put("token", "fallback-token-" + Instant.now().toEpochMilli());
                        // response.put("headerName", "X-XSRF-TOKEN");
                        // response.put("parameterName", "_csrf");
                        return Result.fail(ErrorCode.System.SYSTEM_ERROR);
                    }

                });
    }

    /**
     * 创建一个空的 CsrfToken 用于默认值
     */
    private CsrfToken createEmptyCsrfToken() {
        return new CsrfToken() {
            @Override
            public String getHeaderName() {
                return "X-XSRF-TOKEN";
            }

            @Override
            public String getParameterName() {
                return "_csrf";
            }

            @Override
            public String getToken() {
                return "";
            }
        };
    }
}