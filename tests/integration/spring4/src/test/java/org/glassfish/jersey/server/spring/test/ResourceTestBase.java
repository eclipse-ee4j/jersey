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

package org.glassfish.jersey.server.spring.test;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.SpringLifecycleListener;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.external.ExternalTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * Base class for JAX-RS resource tests.
 *
 * @author Marko Asplund (marko.asplund at yahoo.com)
 */
public abstract class ResourceTestBase extends JerseyTest {

    private static final String TEST_WEBAPP_CONTEXT_PATH = "jersey.spring.test.contextPath";
    private static final String TEST_CONTAINER_FACTORY_EXTERNAL = "org.glassfish.jersey.test.external"
            + ".ExternalTestContainerFactory";

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new ExternalTestContainerFactory();
    }

    @Override
    protected Application configure() {
        final ResourceConfig rc = new ResourceConfig()
                .register(SpringLifecycleListener.class)
                .register(RequestContextFilter.class);
        TestUtil.registerHK2Services(rc);
        rc.property("contextConfigLocation", "classpath:applicationContext.xml");
        return configure(rc);
    }

    protected abstract ResourceConfig configure(ResourceConfig rc);

    protected abstract String getResourcePath();

    protected String getResourceFullPath() {
        final String containerFactory = System.getProperty(TestProperties.CONTAINER_FACTORY);
        if (TEST_CONTAINER_FACTORY_EXTERNAL.equals(containerFactory)) {
            return System.getProperty(TEST_WEBAPP_CONTEXT_PATH) + getResourcePath();
        }
        return getResourcePath();
    }
}
