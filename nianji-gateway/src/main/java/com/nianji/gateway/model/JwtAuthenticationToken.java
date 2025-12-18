package com.nianji.gateway.model;

import com.nianji.common.jwt.dto.JwtUserInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentials;

    public JwtAuthenticationToken(Object principal, String credentials,
                                 Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        if (authorities != null && !authorities.isEmpty()) {
            setAuthenticated(true);
        }
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        if (principal instanceof JwtUserInfo) {
            return ((JwtUserInfo) principal).getUsername();
        }
        return super.getName();
    }
}