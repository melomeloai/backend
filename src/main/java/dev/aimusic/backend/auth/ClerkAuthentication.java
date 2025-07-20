package dev.aimusic.backend.auth;

import dev.aimusic.backend.user.dao.UserModel;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class ClerkAuthentication implements Authentication {
    @Getter
    private final UserModel user;
    private final String token;
    private boolean authenticated = true;

    public ClerkAuthentication(UserModel user, String token) {
        this.user = user;
        this.token = token;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 如果需要角色权限，可以在这里添加
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return user.getClerkId();
    }

    public Long getUserId() {
        return user.getId();
    }
}
