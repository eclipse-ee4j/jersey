/*
 * Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.internal;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.spi.HeaderDelegateProvider;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant;
import javax.ws.rs.ext.RuntimeDelegate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * RuntimeDelegate Decorator that changes behaviour due to provided runtime information.
 * <p/>
 * Currently the following is possible to be configured based on configurable properties:
 * <ul>
 *     <li>{@link org.glassfish.jersey.spi.HeaderDelegateProvider} usage based on
 *     {@link org.glassfish.jersey.CommonProperties#METAINF_SERVICES_LOOKUP_DISABLE}</li>
 * </ul>
 */
public class RuntimeDelegateDecorator {

    /**
     * The decorated {@link RuntimeDelegate} that uses {@link HeaderDelegateProvider HeaderDelegateProviders} based on
     * whether {@link org.glassfish.jersey.CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE} is set or not
     *
     * @param configuration that has or has not {@link org.glassfish.jersey.CommonProperties#FEATURE_AUTO_DISCOVERY_DISABLE} set
     * @return {@link RuntimeDelegate} that uses {@link HeaderDelegateProvider HeaderDelegateProviders}
     */
    public static RuntimeDelegate configured(Configuration configuration) {
        return new ConfigurableRuntimeDelegate(RuntimeDelegate.getInstance(), configuration);
    }

    private static class ConfigurableRuntimeDelegate extends RuntimeDelegate {

        private final RuntimeDelegate runtimeDelegate;
        private final Configuration configuration;
        private static final Set<HeaderDelegateProvider> headerDelegateProviders;

        private ConfigurableRuntimeDelegate(RuntimeDelegate runtimeDelegate, Configuration configuration) {
            this.runtimeDelegate = runtimeDelegate;
            this.configuration = configuration;
        }

        @Override
        public UriBuilder createUriBuilder() {
            return runtimeDelegate.createUriBuilder();
        }

        @Override
        public Response.ResponseBuilder createResponseBuilder() {
            return runtimeDelegate.createResponseBuilder();
        }

        @Override
        public Variant.VariantListBuilder createVariantListBuilder() {
            return runtimeDelegate.createVariantListBuilder();
        }

        @Override
        public <T> T createEndpoint(Application application, Class<T> endpointType)
                throws IllegalArgumentException, UnsupportedOperationException {
            return runtimeDelegate.createEndpoint(application, endpointType);
        }

        @Override
        public <T> RuntimeDelegate.HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
            RuntimeDelegate.HeaderDelegate<T> headerDelegate = null;
            if (configuration == null
                    || PropertiesHelper.isMetaInfServicesEnabled(configuration.getProperties(), configuration.getRuntimeType())) {
                headerDelegate = _createHeaderDelegate(type);
            }
            if (headerDelegate == null) {
                headerDelegate = runtimeDelegate.createHeaderDelegate(type);
            }
            return headerDelegate;
        }

        @Override
        public Link.Builder createLinkBuilder() {
            return runtimeDelegate.createLinkBuilder();
        }

        private <T> HeaderDelegate<T> _createHeaderDelegate(final Class<T> type) {
            for (final HeaderDelegateProvider hp : headerDelegateProviders) {
                if (hp.supports(type)) {
                    return hp;
                }
            }

            return null;
        }

        static {
            Set<HeaderDelegateProvider> hps = new HashSet<HeaderDelegateProvider>();
            for (HeaderDelegateProvider provider : ServiceFinder.find(HeaderDelegateProvider.class, true)) {
                hps.add(provider);
            }
            headerDelegateProviders = Collections.unmodifiableSet(hps);
        }
    }
}
