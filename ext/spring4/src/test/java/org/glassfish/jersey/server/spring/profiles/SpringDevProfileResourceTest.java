/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.server.spring.profiles;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringDevProfileResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        System.setProperty("spring.profiles.active", "dev");
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                "org.glassfish.jersey.server.spring.profiles");
        return new ResourceConfig()
                .register(RequestContextFilter.class)
                .register(LoggingFeature.class)
                .packages("org.glassfish.jersey.server.spring.profiles")
                .property("contextConfig", context);
    }

    @Test
    public void shouldUseDevProfileBean() {
        final String result = target("spring-resource").request().get(String.class);
        Assert.assertEquals("dev", result);
    }
}
