/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

/**
 * Spring WebApplicationInitializer implementation initializes Spring context by
 * adding a Spring ContextLoaderListener to the ServletContext.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public class SpringWebApplicationInitializer implements WebApplicationInitializer {

    private static final Logger LOGGER = Logger.getLogger(SpringWebApplicationInitializer.class.getName());

    private static final String PAR_NAME_CTX_CONFIG_LOCATION = "contextConfigLocation";

    @Override
    public void onStartup(ServletContext sc) throws ServletException {
        if (sc.getInitParameter(PAR_NAME_CTX_CONFIG_LOCATION) == null) {
            LOGGER.config(LocalizationMessages.REGISTERING_CTX_LOADER_LISTENER());
            sc.setInitParameter(PAR_NAME_CTX_CONFIG_LOCATION, "classpath:applicationContext.xml");
            sc.addListener("org.springframework.web.context.ContextLoaderListener");
            sc.addListener("org.springframework.web.context.request.RequestContextListener");
        } else {
            LOGGER.config(LocalizationMessages.SKIPPING_CTX_LODAER_LISTENER_REGISTRATION());
        }
    }
}
