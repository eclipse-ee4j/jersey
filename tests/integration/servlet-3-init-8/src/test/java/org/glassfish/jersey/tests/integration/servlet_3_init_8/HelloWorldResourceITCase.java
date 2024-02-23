/*
 * Copyright (c) 2015, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.integration.servlet_3_init_8;

import java.util.stream.Stream;

import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;

import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test reachable resources.
 *
 * @author Libor Kramolis
 */
public class HelloWorldResourceITCase extends JerseyTest {

    public static Stream<Arguments> testData() {
        return Stream.of(
                //app1 - overridden using a servlet-mapping element in the web.xml
                Arguments.of("/app1", "helloworld1", "World 1"),
                //app2ann - no servlet-mapping in the web.xml, used ApplicationPath.value
                Arguments.of("/app2ann", "helloworld2", "World 2"),
                //app3ann - no servlet in the web.xml, used ApplicationPath.value
                Arguments.of("/app3ann", "helloworld3", "World 3"),
                Arguments.of("/app4", "helloworld4", "World 4"), //app4 - fully configured in web.xml
                //app5 -  automatic registration of all resources, no explicit classes/singletons provided by Servlet3Init8App5
                Arguments.of("/app5", "helloworld1", "World 1"),
                Arguments.of("/app5", "helloworld2", "World 2"),
                Arguments.of("/app5", "helloworld3", "World 3"),
                Arguments.of("/app5", "helloworld4", "World 4"),
                Arguments.of("/app5", "unreachable", "Unreachable")
        );
    }

    @Override
    protected Application configure() {
        return new Application();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @ParameterizedTest(name = "{index}: {0}/{1} \"{2}\"")
    @MethodSource("testData")
    public void testHelloWorld(String appPath, String resourcePath, String helloName) throws Exception {
        WebTarget t = target(appPath);
        t.register(LoggingFeature.class);
        Response r = t.path(resourcePath).request().get();
        assertEquals(200, r.getStatus());
        assertEquals("Hello " + helloName + "!", r.readEntity(String.class));
    }

}
