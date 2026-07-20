/*
 * Copyright (c) 2022, 2026 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.jackson3.internal;

import jakarta.ws.rs.core.Configuration;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson3.LocalizationMessages;
import tools.jackson.databind.AnnotationIntrospector;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.json.JsonMapper;

import org.glassfish.jersey.jackson3.internal.jackson.jakarta.rs.json.JsonMapperConfigurator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class JacksonMapperConfigurator extends JsonMapperConfigurator {

    //do not register JaxbAnnotationModule because it breaks default annotations processing
    private static final String[] EXCLUDE_MODULE_NAMES = {"JaxbAnnotationModule", "JakartaXmlBindAnnotationModule"};
    private static final Logger LOGGER = Logger.getLogger(JacksonMapperConfigurator.class.getName());

    private final Configuration commonConfig;

    public JacksonMapperConfigurator(JsonMapper mapper, AnnotationIntrospector aiOverride, Configuration commonConfig) {
        super(mapper, aiOverride);
        this.commonConfig = commonConfig;
    }

    @Override
    protected MapperBuilder<?, ?> _builderWithConfiguration(MapperBuilder<?, ?> mapperBuilder) {
        MapperBuilder<?, ?> configuredBuilder = super._builderWithConfiguration(mapperBuilder);
        final List<JacksonModule> modules = filterModules(commonConfig, JsonMapper.Builder::findModules);
        return configuredBuilder.removeAllModules().addModules(modules);
    }

    private List<JacksonModule> filterModules(Configuration commonConfig, Supplier<List<JacksonModule>> moduleSupplier) {
        final String disabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_DISABLED_MODULES, String.class);
        final String enabledModules =
                CommonProperties.getValue(commonConfig.getProperties(),
                        commonConfig.getRuntimeType(),
                        CommonProperties.JSON_JACKSON_ENABLED_MODULES, String.class);

        final List<JacksonModule> modules = new ArrayList<>();
        try {
            modules.addAll(moduleSupplier.get());
        } catch (Throwable e) {
            LOGGER.warning(LocalizationMessages.ERROR_MODULES_NOT_LOADED(e.getMessage()));
            return modules;
        }

        for (String exludeModuleName : EXCLUDE_MODULE_NAMES) {
            modules.removeIf(mod -> mod.getModuleName().contains(exludeModuleName));
        }

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
