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

package org.glassfish.jersey.server.spring.aspect4j;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;

public class Aspect4JTest extends JerseyTest {

    private ClassPathXmlApplicationContext applicationContext;

    private TestAspect testAspect;

    @Before
    public void before() {
        testAspect.reset();
    }

    @Override
    protected Application configure() {
        applicationContext = new ClassPathXmlApplicationContext("jersey-spring-aspect4j-applicationContext.xml");
        testAspect = applicationContext.getBean(TestAspect.class);
        return new Aspect4jJerseyConfig()
                .property("contextConfig", applicationContext);
    }

    @Test
    public void methodCallShouldNotBeIntercepted() {
        target("test1").request().get(String.class);
        assertEquals(0, testAspect.getInterceptions());
    }

    @Test
    public void methodCallShouldBeIntercepted() {
        target("test2").request().get(String.class);
        assertEquals(1, applicationContext.getBean(TestAspect.class).getInterceptions());
    }

    @Test
    public void JERSEY_3126() {
        final String result = target("JERSEY-3126").request().get(String.class);
        assertEquals("test ok", result);
    }
}
