/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.auth.jwt.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import org.eclipse.smarthome.io.rest.auth.SmartHomePrincipal;
import org.glassfish.jersey.server.model.AnnotatedMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;

/**
 * This class parses the Swagger annotations for the specification of required scopes,
 * and make sure they figure it the principal's 'scope' claim.
 *
 * @author Yannick Schaus - Initial implementation and API
 *
 */
@Provider
public class RequiredScopesDynamicFeature implements DynamicFeature {

    @Override
    public void configure(final ResourceInfo resourceInfo, final FeatureContext configuration) {
        final AnnotatedMethod am = new AnnotatedMethod(resourceInfo.getResourceMethod());

        if (am.isAnnotationPresent(ApiOperation.class)) {
            ApiOperation op = am.getAnnotation(ApiOperation.class);
            if (op.authorizations() != null) {
                for (Authorization auth : op.authorizations()) {
                    if (auth.scopes() != null) {
                        ArrayList<String> requiredScopes = new ArrayList<String>();
                        for (AuthorizationScope scope : auth.scopes()) {
                            if (!"".equals(scope.scope())) {
                                requiredScopes.add(scope.scope());
                            }
                        }

                        if (requiredScopes.size() > 0) {
                            configuration.register(new RequiredScopesRequestFilter(requiredScopes));
                        }
                    }
                }
            }
            return;
        }
    }

    @Priority(Priorities.AUTHORIZATION) // authorization filter - should go after any authentication filters
    private static class RequiredScopesRequestFilter implements ContainerRequestFilter {

        private final List<String> requiredScopes;

        RequiredScopesRequestFilter(final List<String> requiredScopes) {
            this.requiredScopes = requiredScopes;
        }

        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            if (requestContext.getSecurityContext().getUserPrincipal() == null) {
                return;
            }
            if (!"JWT".equals(requestContext.getSecurityContext().getAuthenticationScheme())) {
                return;
            }

            SmartHomePrincipal user = (SmartHomePrincipal) requestContext.getSecurityContext().getUserPrincipal();
            if (user.getClaims() == null || !user.getClaims().containsKey("scope")) {
                throw new ForbiddenException("Claim 'scope' not found in token");
            }

            List<String> scopes = Arrays.asList(user.getClaims().get("scope").toString().split(" "));

            for (String requiredScope : requiredScopes) {
                if (!scopes.contains(requiredScope)) {
                    throw new ForbiddenException("Missing scope in token : " + requiredScope);
                }
            }
        }
    }
}
