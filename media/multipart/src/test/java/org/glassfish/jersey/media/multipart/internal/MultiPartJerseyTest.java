/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jersey.media.multipart.internal;

import java.util.Set;
import java.util.logging.Logger;

import javax.ws.rs.core.Application;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

/**
 * Common parent class for MultiPart test cases.
 *
 * @author Michal Gajdos
 */
abstract class MultiPartJerseyTest extends JerseyTest {

    private static final Logger LOGGER = Logger.getLogger(MultiPartJerseyTest.class.getName());

    @Override
    protected Application configure() {
        enable(TestProperties.LOG_TRAFFIC);
        enable(TestProperties.DUMP_ENTITY);

        return new ResourceConfig()
                .registerClasses(getResourceClasses())
                .registerClasses(MultiPartBeanProvider.class)
                .registerInstances(new LoggingFeature(LOGGER, LoggingFeature.Verbosity.PAYLOAD_ANY))
                .register(new MultiPartFeature());
    }

    /**
     * Returns a set of resource classes needed by this test.
     *
     * @return set of resource classes.
     */
    protected abstract Set<Class<?>> getResourceClasses();

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(MultiPartFeature.class).register(MultiPartBeanProvider.class);
    }

}
