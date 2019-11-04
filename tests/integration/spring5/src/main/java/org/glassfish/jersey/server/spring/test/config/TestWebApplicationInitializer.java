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

package org.glassfish.jersey.server.spring.test.config;

import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class TestWebApplicationInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup(ServletContext servletContext) {
        servletContext.addListener(ContextLoaderListener.class);
        servletContext.addListener(RequestContextListener.class);

        servletContext.setInitParameter(ContextLoader.CONTEXT_CLASS_PARAM,
                AnnotationConfigWebApplicationContext.class.getName());
        servletContext.setInitParameter(ContextLoader.CONFIG_LOCATION_PARAM,
                TestApplicationConfig.class.getName());

        ServletRegistration.Dynamic dynamic = servletContext.addServlet(
                "test-servlet", ServletContainer.class.getName());
        dynamic.setInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, TestApplicationConfig.class.getName());
        dynamic.setLoadOnStartup(1);
        dynamic.addMapping("/*");
    }

}
