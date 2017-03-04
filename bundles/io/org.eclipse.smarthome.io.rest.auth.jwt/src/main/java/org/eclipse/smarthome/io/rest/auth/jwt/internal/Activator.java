/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.rest.auth.jwt.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Registers the scope checking dynamic feature.
 *
 * @author Yannick Schaus - Initial contribution and API
 *
 */

public class Activator implements BundleActivator {

    private ServiceRegistration requiredScopesDynamicFeatureRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        registerProviderServices(context);
    }

    private void registerProviderServices(BundleContext context) {
        requiredScopesDynamicFeatureRegistration = context.registerService(RequiredScopesDynamicFeature.class.getName(),
                new RequiredScopesDynamicFeature(), null);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        unregisterProviderServices();
    }

    private void unregisterProviderServices() {
        if (requiredScopesDynamicFeatureRegistration != null) {
            requiredScopesDynamicFeatureRegistration.unregister();
        }
    }
}
