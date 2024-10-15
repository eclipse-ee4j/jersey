package org.glassfish.jersey.examples.security.authorization;


import org.glassfish.jersey.examples.security.model.Role;
import org.glassfish.jersey.examples.settings.SecuritySettings;
import io.jsonwebtoken.*;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Provider
@PreMatching
public class PrincipalInterceptor implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        final String authorization = requestContext.getHeaderString(SecuritySettings.AUTHORIZATION_HEADER);

        if (null == authorization) {
            requestContext.setSecurityContext(new UserSecurityContext(SecuritySettings.UNKNOWN_USER));
            return;
        }

        final int schemeLength = SecuritySettings.AUTHORIZATION_SCHEME.length();
        final int tokenLength = authorization.length();

        if (!(authorization.contains(SecuritySettings.AUTHORIZATION_SCHEME)
                && tokenLength > schemeLength)) {
            return;
        }

        try {
            int tokenSize = tokenLength - schemeLength;
            char[] token = new char[tokenSize - 1];

            authorization.getChars(schemeLength + 1, tokenLength, token, 0);

            final String verifyToken = new String(token);

            final Jws<Claims> claimsJws = Jwts.parser()
                    .requireIssuer(SecuritySettings.JWT_ISSUER)
                    .setSigningKey(SecuritySettings.JWT_SECRETE)
                    .parseClaimsJws(verifyToken);

            final Claims claimsBody = claimsJws.getBody();

            final String username = claimsBody.getSubject();
            final Object roles = claimsBody.get(SecuritySettings.JWT_ROLES_CLAIM, Object.class);

            final UserSecurityContext userSecurityContext = new UserSecurityContext(username);

            if (roles instanceof ArrayList) {
                List<String> roleList = (List<String>) roles;
                final List<Role> roleList1 = roleList.stream().map(Role::new).collect(Collectors.toList());
                userSecurityContext.addRole(roleList1);
            }

            requestContext.setSecurityContext(userSecurityContext);

        } catch (ExpiredJwtException e) {

            requestContext.abortWith(Response.status(Response.Status.FORBIDDEN).build());
        } catch (UnsupportedJwtException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        } catch (MalformedJwtException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        } catch (SignatureException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        } catch (IllegalArgumentException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        } catch (IncorrectClaimException e) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        }


    }


    public static class UserSecurityContext implements SecurityContext, Principal {

        private final String principal;
        private final List<Role> roles;

        public UserSecurityContext(final String principal, Role... roles) {
            this.principal = principal;
            this.roles = new ArrayList<>();
            this.roles.addAll(Arrays.asList(roles));
        }

        public void addRole(List<Role> roles) {
            this.roles.addAll(roles);
        }

        @Override
        public Principal getUserPrincipal() {
            return this;
        }

        @Override
        public boolean isUserInRole(final String role) {
            return this.roles.stream().anyMatch(e -> e.getName().equalsIgnoreCase(role));
        }

        @Override
        public boolean isSecure() {
            return false;
        }

        @Override
        public final String getAuthenticationScheme() {
            return SecuritySettings.AUTHORIZATION_SCHEME;
        }

        @Override
        public String getName() {
            return this.principal;
        }
    }
}
