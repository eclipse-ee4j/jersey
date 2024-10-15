/*
 * Copyright (c) 2017, 2024 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jsonb;

import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import org.glassfish.jersey.ApplicationSupplier;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.internal.InternalProperties;
import org.glassfish.jersey.internal.inject.InjectionManagerSupplier;
import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.jsonb.internal.JsonBindingAutoDiscoverable;
import org.glassfish.jersey.jsonb.internal.JsonBindingProvider;

import java.security.AccessController;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Feature used to register JSON-B providers.
 * <p>
 * The Feature is automatically enabled when {@link JsonBindingAutoDiscoverable} is on classpath.
 * Default JSON-B configuration obtained by calling {@code JsonbBuilder.create()} is used.
 * <p>
 * Custom configuration, if required, can be achieved by implementing custom {@link jakarta.ws.rs.ext.ContextResolver} and
 * registering it as a provider into JAX-RS runtime:
 * <pre>
 * &#64;Provider
 * &#64;class JsonbContextResolver implements ContextResolver&lt;Jsonb&gt; {
 *      &#64;Override
 *      public Jsonb getContext(Class<?> type) {
 *          JsonbConfig config = new JsonbConfig();
 *          // add custom configuration
 *          return JsonbBuilder.create(config);
 *      }
 * }
 * </pre>
 *
 * @author Adam Lindenthal
 */
public class JsonBindingFeature implements Feature {

    private static final Logger LOGGER = Logger.getLogger(JsonBindingFeature.class.getName());
    private static final String JSON_FEATURE = JsonBindingFeature.class.getSimpleName();

    @Override
    public boolean configure(final FeatureContext context) {
        final Configuration config = context.getConfiguration();

        // ---- Allow to disable for compatibility with Pre JAX-RS 2.1 Jersey.

        /* Either system properties */
        final String bindingDisabledBySystemProperty = AccessController.doPrivileged(
                PropertiesHelper.getSystemProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE));

        final String bindingDisabledBySystemPropertyClient = AccessController.doPrivileged(
                PropertiesHelper.getSystemProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_CLIENT));

        final String bindingDisabledBySystemPropertyServer = AccessController.doPrivileged(
                PropertiesHelper.getSystemProperty(CommonProperties.JSON_BINDING_FEATURE_DISABLE_SERVER));

        final RuntimeType runtimeType = config.getRuntimeType();

        boolean bindingDisabledBySystem = PropertiesHelper.isPropertyOrNotSet(bindingDisabledBySystemProperty)
                || (runtimeType == RuntimeType.CLIENT
                    && PropertiesHelper.isPropertyOrNotSet(bindingDisabledBySystemPropertyClient))
                || (runtimeType == RuntimeType.SERVER
                    && PropertiesHelper.isPropertyOrNotSet(bindingDisabledBySystemPropertyServer));

        /* Or config property */
        final Boolean bindingDisabled = CommonProperties.getValue(config.getProperties(), runtimeType,
                CommonProperties.JSON_BINDING_FEATURE_DISABLE, Boolean.class);

        /* Config property takes precedence */
        if ((bindingDisabledBySystem && !Boolean.FALSE.equals(bindingDisabled)) || Boolean.TRUE.equals(bindingDisabled)) {
            return false;
        }

        final Set<String> disabledPackageNames = new HashSet<>();

        /* Only a certain package names */
        final String bindingDisabledPackageBySystemProperty = RuntimeType.SERVER == runtimeType
                ? AccessController.doPrivileged(PropertiesHelper.getSystemProperty(
                        CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION))
                : null;

        final String bindingDisabledPackage = RuntimeType.SERVER == runtimeType
                ? CommonProperties.getValue(config.getProperties(), runtimeType,
                        CommonProperties.JSON_BINDING_FEATURE_DISABLE_APPLICATION, String.class)
                : null;

        separatePackageNames(disabledPackageNames, bindingDisabledPackageBySystemProperty);
        separatePackageNames(disabledPackageNames, bindingDisabledPackage);

        if (!disabledPackageNames.isEmpty() && !Boolean.FALSE.equals(bindingDisabled)) {
            try {
                Application app = null;
                if (InjectionManagerSupplier.class.isInstance(context)) {
                    app = ((InjectionManagerSupplier) context).getInjectionManager().getInstance(Application.class);
                    if (app != null) {
                        while (ApplicationSupplier.class.isInstance(app) && ((ApplicationSupplier) app).getApplication() != app) {
                            app = ((ApplicationSupplier) app).getApplication();
                        }
                        for (String disabledPackageName : disabledPackageNames) {
                            if (app.getClass().getName().startsWith(disabledPackageName)) {
                                return false;
                            }
                        }
                    }
                }
                if (app == null) {
                    LOGGER.warning(LocalizationMessages.ERROR_JSONB_DETECTING_APPLICATION(
                            LocalizationMessages.ERROR_JSONB_APPLICATION_NOT_FOUND()));
                }
            } catch (Throwable throwable) {
                LOGGER.warning(LocalizationMessages.ERROR_JSONB_DETECTING_APPLICATION(throwable.getMessage()));
            }
        }

        // ---- End of disabling for compatibility with Pre JAX-RS 2.1 Jersey.

        final String jsonFeature = CommonProperties.getValue(
                config.getProperties(),
                config.getRuntimeType(),
                InternalProperties.JSON_FEATURE, JSON_FEATURE, String.class);

        // Other JSON providers registered.
        if (!JSON_FEATURE.equalsIgnoreCase(jsonFeature)) {
            return false;
        }

        // Disable other JSON providers.
        context.property(PropertiesHelper.getPropertyNameForRuntime(
                InternalProperties.JSON_FEATURE, config.getRuntimeType()), JSON_FEATURE);

        context.register(JsonBindingProvider.class);

        return true;
    }

    private static void separatePackageNames(Set<String> set, String packages) {
        if (packages != null) {
            for (String packageName : packages.split(",")) {
                set.add(packageName.trim());
            }
        }
    }
}
