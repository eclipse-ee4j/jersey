/*
 * Copyright (c) 2014, 2023 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.tests.e2e.container;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.glassfish.jersey.internal.util.JdkVersion;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.jdkhttp.JdkHttpServerTestContainerFactory;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.netty.NettyTestContainerFactory;
import org.glassfish.jersey.test.simple.SimpleTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * @author Michal Gajdos
 */
public abstract class JerseyContainerTest extends JerseyTest {

    private static final List<TestContainerFactory> FACTORIES = listContainerFactories(
            new GrizzlyTestContainerFactory(),
            new InMemoryTestContainerFactory(),
            new SimpleTestContainerFactory(),
            new JdkHttpServerTestContainerFactory(),
            new JettyTestContainerFactory(),
            new NettyTestContainerFactory()
    );

    public static Stream<TestContainerFactory> parameters() {
        return FACTORIES.stream();
    }

    public JerseyContainerTest(TestContainerFactory testContainerFactory) {
        super(testContainerFactory);
    }

    protected static List<TestContainerFactory> listContainerFactories(TestContainerFactory... factories) {
        final JdkVersion version = JdkVersion.getJdkVersion();
        boolean isJDKGreaterThanOrEqualTo17 = version.getMajor() >= 17;
        final List<TestContainerFactory> filtered = new LinkedList<>();
        for (TestContainerFactory factory : factories) {
            if (isJDKGreaterThanOrEqualTo17 || !JettyTestContainerFactory.class.isInstance(factory)) {
                filtered.add(factory);
            }
        }
        return filtered;
    }
}
