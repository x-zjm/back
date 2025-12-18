package com.nianji.common.jwt.exception;

public class JwtGenerationException extends JwtException {
    public JwtGenerationException(String message) {
        super("JWT_GENERATION_ERROR", message);
    }
    
    public JwtGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}