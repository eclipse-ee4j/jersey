/*
 * Copyright (c) 2020, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.Module;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.Annotations;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Providers;

/**
 * Entity Data provider based on Jackson JSON provider.
 */
@Singleton
public class DefaultJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {
    private Configuration commonConfig;

    @Inject
    public DefaultJacksonJaxbJsonProvider(@Context Providers providers, @Context Configuration config) {
        this.commonConfig = config;
        _providers = providers;
    }

    //do not register JaxbAnnotationModule because it brakes default annotations processing
    private static final String EXCLUDE_MODULE_NAME = "JaxbAnnotationModule";

    public DefaultJacksonJaxbJsonProvider(Providers providers, Configuration config, Annotations... annotationsToUse) {
        super(annotationsToUse);
        this.commonConfig = config;
        _providers = providers;
    }

    @PostConstruct
    private void findAndRegisterModules() {

        final ObjectMapper defaultMapper = _mapperConfig.getDefaultMapper();
        final ObjectMapper mapper = _mapperConfig.getConfiguredMapper();

        final List<Module> modules = filterModules();

        defaultMapper.registerModules(modules);
        if (mapper != null) {
            mapper.registerModules(modules);
        }
    }

    private List<Module> filterModules() {
        final String disabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_DISABLED_MODULES, String.class);
        final String enabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_ENABLED_MODULES, String.class);

        final List<Module> modules = ObjectMapper.findModules();
        modules.removeIf(mod -> mod.getModuleName().contains(EXCLUDE_MODULE_NAME));

        if (enabledModules != null && !enabledModules.isEmpty()) {
            final List<String> enabledModulesList = Arrays.asList(enabledModules.split(","));
            modules.removeIf(mod -> !enabledModulesList.contains(mod.getModuleName()));
        } else if (disabledModules != null && !disabledModules.isEmpty()) {
            final List<String> disabledModulesList = Arrays.asList(disabledModules.split(","));
            modules.removeIf(mod -> disabledModulesList.contains(mod.getModuleName()));
        }

        return modules;
    }
}