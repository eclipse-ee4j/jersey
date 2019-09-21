/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.jersey.examples.hello.spring.annotations;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Testing our service with our annotation context being passed directly to jersey-spring
 *
 * @author Geoffroy Warin (http://geowarin.github.io)
 */
public class SpringRequestResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        ApplicationContext context = new AnnotationConfigApplicationContext(SpringAnnotationConfig.class);
        return new JerseyConfig().property("contextConfig", context);
    }

    @Test
    public void testGreet() throws Exception {
        final String greeting = target("spring-resource").request().get(String.class);
        Assert.assertEquals("hello, world 1!", greeting);
        final String greeting2 = target("spring-resource").request().get(String.class);
        Assert.assertEquals("hello, world 2!", greeting2);
    }

    @Test
    public void testGoodbye() {
        final String goodbye = target("spring-resource").path("goodbye").request().get(String.class);
        Assert.assertEquals("goodbye, cruel world!", goodbye);
        final String norwegianGoodbye = target("spring-resource").path("norwegian-goodbye").request().get(String.class);
        Assert.assertEquals("hadet, på badet!", norwegianGoodbye);
    }
}
