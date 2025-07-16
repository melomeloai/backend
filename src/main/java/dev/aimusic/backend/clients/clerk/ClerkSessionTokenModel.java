package dev.aimusic.backend.clients.clerk;

import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Set;

@Data
@Builder
public class ClerkSessionTokenModel {
    private String sub;
    private String email;
    private String name;
    private String issuer;
    private Date issuedAt;
    private Date expiresAt;
    private Set<String> audience;
}
