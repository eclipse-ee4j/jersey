/*
 * Copyright (c) 2020, 2021 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.Annotations;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import java.util.List;
import javax.inject.Singleton;

/**
 * Entity Data provider based on Jackson JSON provider.
 */
@Singleton
public class DefaultJacksonJaxbJsonProvider extends JacksonJaxbJsonProvider {

    //do not register JaxbAnnotationModule because it brakes default annotations processing
    private static final String EXCLUDE_MODULE_NAME = "JaxbAnnotationModule";

    public DefaultJacksonJaxbJsonProvider() {
        super();
        findAndRegisterModules();
    }

    public DefaultJacksonJaxbJsonProvider(final Annotations... annotationsToUse) {
        super(annotationsToUse);
        findAndRegisterModules();
    }

    private void findAndRegisterModules() {

        final ObjectMapper defaultMapper = _mapperConfig.getDefaultMapper();
        final ObjectMapper mapper = _mapperConfig.getConfiguredMapper();

        final List<Module> modules = ObjectMapper.findModules();
        modules.removeIf(mod -> mod.getModuleName().contains(EXCLUDE_MODULE_NAME));

        defaultMapper.registerModules(modules);
        if (mapper != null) {
            mapper.registerModules(modules);
        }
    }
}