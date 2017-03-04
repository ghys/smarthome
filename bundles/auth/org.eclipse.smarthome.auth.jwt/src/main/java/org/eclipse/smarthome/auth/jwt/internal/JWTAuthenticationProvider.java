/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.auth.jwt.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.smarthome.core.auth.Authentication;
import org.eclipse.smarthome.core.auth.AuthenticationException;
import org.eclipse.smarthome.core.auth.AuthenticationProvider;
import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.core.auth.TokenCredentials;
import org.jose4j.base64url.Base64;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

/**
 * Implementation of an authentication provider using "blindly" the claims found in a
 * JSON Web Token issued by a trusted identity provider.
 *
 * It verifies the signature of the token using a pre-shared key and expects at least a
 * 'sub' claim and one or more 'role' claims to ultimately build a SmartHomePrincipal. All claims found
 * in the token will also be included, so that authorization checks could made against any of these claims.
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class JWTAuthenticationProvider implements AuthenticationProvider {

    private String trustedIssuer;
    private byte[] trustedKey;

    @Override
    public Authentication authenticate(final Credentials credentials) {
        String jwt = getToken(credentials);
        try {
            JwtClaims claims = verifyAndProcessToken(jwt);

            String subject = claims.getSubject();

            List<String> roles;
            try {
                // try a single role first
                String role = claims.getStringClaimValue("role");
                roles = Collections.singletonList(role);
            } catch (MalformedClaimException e) {
                try {
                    roles = claims.getStringListClaimValue("role");
                } catch (MalformedClaimException e2) {
                    throw new AuthenticationException("No role claim found in token");
                }
            }

            return new Authentication(subject, (roles != null) ? roles.toArray(new String[0]) : null,
                    claims.getClaimsMap());

        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }

    }

    private String getToken(Credentials credentials) {
        if (credentials instanceof TokenCredentials) {
            return ((TokenCredentials) credentials).getToken();
        }
        return null;
    }

    private HmacKey getKey() {
        HmacKey key = new HmacKey(this.trustedKey);
        return key;
    }

    private String getTrustedIssuer() {
        return this.trustedIssuer;
    }

    private JwtClaims verifyAndProcessToken(String jwt) throws InvalidJwtException {
        JwtConsumer jwtConsumer = new JwtConsumerBuilder() // .setRequireExpirationTime() // the JWT must have an expiration time
                // .setMaxFutureValidityInMinutes(1440) // but the expiration time can't be too crazy
                // .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireSubject() // the JWT must have a subject claim
                .setExpectedIssuer(getTrustedIssuer()) // whom the JWT needs to have been issued by
                // .setExpectedAudience("smarthome") // to whom the JWT is intended for
                .setVerificationKey(getKey()) // verify the signature with the pre-shared key
                .setRelaxVerificationKeyValidation() // don't verify key length yet - @TODO remove!
                .build(); // create the JwtConsumer instance

        return jwtConsumer.processToClaims(jwt);
    }

    protected void activate(Map<String, Object> properties) {
        modified(properties);
    }

    protected void deactivate(Map<String, Object> properties) {
    }

    protected void modified(Map<String, Object> properties) {
        if (properties == null) {
            return;
        }

        Object trustedIssuerName = properties.get("trustedIssuer");
        if (trustedIssuerName != null) {
            if (trustedIssuerName instanceof String) {
                this.trustedIssuer = (String) trustedIssuerName;
            }
        }

        // @TODO support asymmetric keys
        Object base64EncodedPreSharedKey = properties.get("preSharedKey");
        if (base64EncodedPreSharedKey != null) {
            if (base64EncodedPreSharedKey instanceof String) {
                this.trustedKey = Base64.decode((String) base64EncodedPreSharedKey);
            }
        }
    }
}
