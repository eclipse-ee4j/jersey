/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.wadl.config;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ProcessingException;

import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.internal.LocalizationMessages;

/**
 * Loads a {@link WadlGeneratorConfig} and provides access to the {@link org.glassfish.jersey.server.wadl.WadlGenerator}
 * provided by the loaded {@link WadlGeneratorConfig}.<br/>
 * If no {@link WadlGeneratorConfig} is provided, the default {@link org.glassfish.jersey.server.wadl.WadlGenerator}
 * will be loaded.<br />
 *
 * @author Martin Grotzke (martin.grotzke at freiheit.com)
 */
public class WadlGeneratorConfigLoader {

    /**
     * Load the {@link WadlGeneratorConfig} from the provided {@link org.glassfish.jersey.server.ResourceConfig} using the
     * property {@link org.glassfish.jersey.server.ServerProperties#WADL_GENERATOR_CONFIG}.
     *
     * <p>
     * The type of this property must be a subclass or an instance of a subclass of
     * {@link WadlGeneratorConfig}.<br/>
     * If it's not set, the default {@link org.glassfish.jersey.server.wadl.internal.generators.WadlGeneratorJAXBGrammarGenerator}
     * will be used.
     * </p>
     *
     * @param properties configuration properties of deployed Jersey application.
     * @return a configure {@link WadlGeneratorConfig}.
     */
    public static WadlGeneratorConfig loadWadlGeneratorsFromConfig(final Map<String, Object> properties) {
        final Object wadlGeneratorConfigProperty = properties.get(
                ServerProperties.WADL_GENERATOR_CONFIG);
        if (wadlGeneratorConfigProperty == null) {
            return new WadlGeneratorConfig() {
                @Override
                public List<WadlGeneratorDescription> configure() {
                    return Collections.emptyList();
                }
            };
        } else {

            try {

                if (wadlGeneratorConfigProperty instanceof WadlGeneratorConfig) {
                    return ((WadlGeneratorConfig) wadlGeneratorConfigProperty);
                }

                final Class<? extends WadlGeneratorConfig> configClazz;
                if (wadlGeneratorConfigProperty instanceof Class) {
                    configClazz = ((Class<?>) wadlGeneratorConfigProperty)
                            .asSubclass(WadlGeneratorConfig.class);
                } else if (wadlGeneratorConfigProperty instanceof String) {
                    configClazz = AccessController.doPrivileged(ReflectionHelper
                            .classForNameWithExceptionPEA((String) wadlGeneratorConfigProperty))
                            .asSubclass(WadlGeneratorConfig.class);
                } else {
                    throw new ProcessingException(LocalizationMessages.ERROR_WADL_GENERATOR_CONFIG_LOADER_PROPERTY(
                            ServerProperties.WADL_GENERATOR_CONFIG,
                            wadlGeneratorConfigProperty.getClass().getName()));
                }
                return configClazz.newInstance();

            } catch (final PrivilegedActionException pae) {
                throw new ProcessingException(LocalizationMessages.ERROR_WADL_GENERATOR_CONFIG_LOADER(
                        ServerProperties.WADL_GENERATOR_CONFIG), pae.getCause());
            } catch (final Exception e) {
                throw new ProcessingException(LocalizationMessages.ERROR_WADL_GENERATOR_CONFIG_LOADER(
                        ServerProperties.WADL_GENERATOR_CONFIG), e);
            }
        }
    }

}
