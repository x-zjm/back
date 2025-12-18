package com.nianji.common.jwt.exception;

public class JwtException extends RuntimeException {
    private final String code;

    public JwtException(String message) {
        super(message);
        this.code = "JWT_ERROR";
    }

    public JwtException(String code, String message) {
        super(message);
        this.code = code;
    }

    public JwtException(String message, Throwable cause) {
        super(message, cause);
        this.code = "JWT_ERROR";
    }

    public String getCode() {
        return code;
    }
}