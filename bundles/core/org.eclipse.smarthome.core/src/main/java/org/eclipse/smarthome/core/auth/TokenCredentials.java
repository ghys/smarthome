/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.auth;

/**
 * Credentials consisting of a self-contained token to be handled
 * by an AuthenticationProvider which understands it.
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */
public class TokenCredentials implements Credentials {

    private final String token;

    /**
     * Creates a new instance
     *
     * @param a token containing the credentials of the user
     */
    public TokenCredentials(String token) {
        this.token = token;
    }

    /**
     * Retrieves the token
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

}
