/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Jersey test framework common classes that support testing JAX-RS and Jersey-based applications.
 *
 * The {@link org.glassfish.jersey.test.JerseyTest} class may be extended to define the testing
 * configuration and functionality.
 * <p>
 * For example, the following class is configured to use Grizzly HTTP test container factory,
 * {@code org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory} and test that a simple resource
 * {@code TestResource} returns the expected results for a HTTP GET request:
 * </p>
 * <pre>
 * public class SimpleGrizzlyBasedTest extends JerseyTest {
 *
 *   &#64;Path("root")
 *   public static class TestResource {
 *     &#64;GET
 *     public String get() {
 *       return "GET";
 *     }
 *   }
 *
 *   &#64;Override
 *   protected Application configure() {
 *     enable(TestProperties.LOG_TRAFFIC);
 *     return new ResourceConfig(TestResource.class);
 *   }
 *
 *   &#64;Override
 *   protected TestContainerFactory getTestContainerFactory() {
 *     return new GrizzlyTestContainerFactory();
 *   }
 *
 *   &#64;Test
 *   public void testGet() {
 *     WebTarget t = target("root");
 *
 *     String s = t.request().get(String.class);
 *     Assert.assertEquals("GET", s);
 *   }
 * }
 * </pre>
 * <p>
 * The following tests the same functionality using the Servlet-based Grizzly test container factory,
 * {@code org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory}:
 * </p>
 * <pre>
 * public class WebBasedTest extends JerseyTest {
 *
 *   &#64;Path("root")
 *   public static class TestResource {
 *     &#64;GET
 *     public String get() {
 *       return "GET";
 *     }
 *   }
 *
 *   &#64;Override
 *   protected DeploymentContext configureDeployment() {
 *     return ServletDeploymentContext.builder("foo")
 *             .contextPath("context").build();
 *   }
 *
 *   &#64;Override
 *   protected TestContainerFactory getTestContainerFactory() {
 *     return new GrizzlyTestContainerFactory();
 *   }
 *
 *   &#64;Test
 *   public void testGet() {
 *     WebTarget t = target("root");
 *
 *     String s = t.request().get(String.class);
 *     Assert.assertEquals("GET", s);
 *   }
 * }
 * </pre>
 * <p>
 * The above test is actually not specific to any Servlet-based test container factory and will work for all
 * provided test container factories. See the documentation on {@link org.glassfish.jersey.test.JerseyTest} for
 * more details on how to set the default test container factory.
 * </p>
 */
package org.glassfish.jersey.test;
