package com.kevindubois.security;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

/**
 * Test-only identity provider so the admin MCP tools (which are @RolesAllowed("admin")) can be
 * exercised in @QuarkusTest without a real Keycloak realm. Authenticates basic auth admin/admin.
 */
@ApplicationScoped
public class TestAdminIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

    @Override
    public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
        return UsernamePasswordAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request,
            AuthenticationRequestContext context) {
        String user = request.getUsername();
        PasswordCredential password = request.getPassword();
        String secret = password != null ? new String(password.getPassword()) : null;
        if ("admin".equals(user) && "admin".equals(secret)) {
            return Uni.createFrom().item(QuarkusSecurityIdentity.builder()
                .setPrincipal(new QuarkusPrincipal(user))
                .addRoles(Set.of("admin"))
                .build());
        }
        return Uni.createFrom().failure(new AuthenticationFailedException());
    }
}
