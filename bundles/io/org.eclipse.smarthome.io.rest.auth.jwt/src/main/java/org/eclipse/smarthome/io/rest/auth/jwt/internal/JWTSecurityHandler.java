/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.auth.jwt.internal;

import java.security.Principal;
import java.util.StringTokenizer;

import javax.ws.rs.container.ContainerRequestContext;

import org.eclipse.smarthome.core.auth.Credentials;
import org.eclipse.smarthome.core.auth.TokenCredentials;
import org.eclipse.smarthome.io.rest.auth.AbstractSecurityHandler;
import org.eclipse.smarthome.io.rest.auth.SmartHomePrincipal;

/**
 * Handler responsible for parsing self-contained JSON Web Tokens sent over standard http header as access tokens.
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class JWTSecurityHandler extends AbstractSecurityHandler {

    @Override
    protected Credentials createCredentials(ContainerRequestContext requestContext) {
        String authenticationHeader = requestContext.getHeaderString("Authorization");

        if (authenticationHeader == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(authenticationHeader, " ");
        String authType = tokenizer.nextToken();
        if ("Bearer".equalsIgnoreCase(authType)) {
            String jwtToken = tokenizer.nextToken();

            return new TokenCredentials(jwtToken);
        }

        return null;
    }

    @Override
    public String getAuthenticationScheme() {
        return "JWT";
    }

}
