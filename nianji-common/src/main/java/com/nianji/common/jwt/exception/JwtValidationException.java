package com.nianji.common.jwt.exception;

public class JwtValidationException extends JwtException {
    public JwtValidationException(String message) {
        super("JWT_VALIDATION_ERROR", message);
    }

    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}