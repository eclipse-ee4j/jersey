/*
 * Copyright (c) 2025 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * minimalistic test to demonstrate Spring/servlet integration
 */

public class ServletContainerIntegrationTest extends JerseyTest {

    @Override
    protected DeploymentContext configureDeployment() {
        // Set up Spring context
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(SpringServletService.class);
        context.refresh();

        final ResourceConfig config = new ResourceConfig(ServletSpringResource.class);
        config.property("contextConfig", context);

        return ServletDeploymentContext
                .builder(config)
                .servlet(new ServletContainer(config))
                .addListener(ContextLoaderListener.class)
                .build();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        // Use Grizzly with servlet support to enable HttpServletRequest
        return new GrizzlyWebTestContainerFactory();
    }

    @Test
    public void servletContainerRequestTest() {
        Response response = target("/test").request().get();
        assertEquals(200, response.getStatus());
    }

}
